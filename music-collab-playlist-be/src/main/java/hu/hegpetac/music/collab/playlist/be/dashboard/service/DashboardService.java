package hu.hegpetac.music.collab.playlist.be.dashboard.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.dashboard.mapper.DashboardMapper;
import hu.hegpetac.music.collab.playlist.be.dashboard.repository.DashboardSettingsRepository;
import hu.hegpetac.music.collab.playlist.be.exception.BadRequestException;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Value("${FRONTEND_BASEURL}")
    private String frontendBaseUrl;

    private final DashboardSettingsRepository dashboardSettingsRepository;
    private final DashboardMapper dashboardMapper;
    private final Random random = new Random();

    public org.openapitools.model.DashboardSettings getDashboardSettings() throws UnauthorizedException, NotFoundException {
        System.out.println("Getting dashboard settings for authenticated user");
        DashboardSettings dashboardSettings = getDashboardFromAuthenticatedUserFromSession();

        if (dashboardSettings.getDeviceCode() == null) {
            dashboardSettings = generateDeviceCode(dashboardSettings);
        }

        org.openapitools.model.DashboardSettings dashboardModel = dashboardMapper.mapDashboardSettings(dashboardSettings);
        return generateQRCodeUrl(dashboardModel);
    }

    public RegenerateCodeResp regenerateDeviceCode() throws UnauthorizedException {
        DashboardSettings dashboardSettings = getDashboardFromAuthenticatedUserFromSession();

        dashboardSettings = generateDeviceCode(dashboardSettings);
        org.openapitools.model.DashboardSettings dashboardModel = dashboardMapper.mapDashboardSettings(dashboardSettings);
        dashboardModel = generateQRCodeUrl(dashboardModel);

        return new RegenerateCodeResp(dashboardModel.getDeviceCode(), dashboardModel.getQrBaseUrl());
    }

    public org.openapitools.model.SuggestionPlaybackMode modifySuggestionPlaybackMode(ModifySuggestionPlaybackModeReq modifySuggestionPlaybackModeReq) throws UnauthorizedException, NotFoundException {
        DashboardSettings dashboardSettings = getDashboardFromAuthenticatedUserFromSession();

        dashboardSettings.setSuggestionPlaybackMode(
                dashboardMapper.mapSuggestionPlaybackMode(modifySuggestionPlaybackModeReq.getSuggestionPlaybackMode())
        );
        dashboardSettings = dashboardSettingsRepository.save(dashboardSettings);

        return dashboardMapper.mapSuggestionPlaybackMode(dashboardSettings.getSuggestionPlaybackMode());
    }

    public org.openapitools.model.YoutubePlaybackMode modifyYoutubePlaybackMode(ModifyYoutubePlaybackModeReq modifyYoutubePlaybackModeReq) throws UnauthorizedException, NotFoundException {
        DashboardSettings dashboardSettings = getDashboardFromAuthenticatedUserFromSession();

        dashboardSettings.setYoutubePlaybackMode(
                dashboardMapper.mapYoutubePlaybackMode(modifyYoutubePlaybackModeReq.getYoutubePlaybackMode())
        );
        dashboardSettings = dashboardSettingsRepository.save(dashboardSettings);

        return dashboardMapper.mapYoutubePlaybackMode(dashboardSettings.getYoutubePlaybackMode());
    }

    public ModifyNameResp modifyName(ModifyNameReq modifyNameReq) throws UnauthorizedException, NotFoundException, BadRequestException {
        DashboardSettings dashboardSettings = getDashboardFromAuthenticatedUserFromSession();
        dashboardSettingsRepository.findByName(modifyNameReq.getName())
                .ifPresent(_ -> {
                    throw new BadRequestException("Name already exists: " + modifyNameReq.getName());
                });

        dashboardSettings.setName(modifyNameReq.getName());
        dashboardSettings = dashboardSettingsRepository.save(dashboardSettings);

        return new ModifyNameResp(dashboardSettings.getName());
    }

    private DashboardSettings getDashboardFromAuthenticatedUserFromSession() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User user = customOAuth2User.getUser();

                Optional<DashboardSettings> existingDashboardSettings = dashboardSettingsRepository.findByUser(user);
                if (existingDashboardSettings.isEmpty()) {
                    throw new NotFoundException("No settings found for user " + user.getId());
                }
                DashboardSettings dashboardSettings = existingDashboardSettings.get();
                dashboardSettings.setSpotifyAccountDisplayName(user.getDisplayName());
                dashboardSettings.setGoogleAccountEmail(user.getEmail());

                return dashboardSettings;
            }
        }

        throw new UnauthorizedException("No authenticated user found in session");
    }

    private DashboardSettings generateDeviceCode(DashboardSettings dashboardSettings) {
        dashboardSettings.setDeviceCode(random.nextInt (900000) + 100000);
        return dashboardSettingsRepository.save(dashboardSettings);
    }

    private org.openapitools.model.DashboardSettings generateQRCodeUrl(org.openapitools.model.DashboardSettings dashboardSettings) {
        String QRCodeUrl = frontendBaseUrl + "/suggestion-dashboard?name=" + dashboardSettings.getName() + "&deviceCode=" + dashboardSettings.getDeviceCode();
        try {
            dashboardSettings.setQrBaseUrl(new URI(QRCodeUrl));
        } catch (URISyntaxException e) {
            throw new BadRequestException("URI creation failed for: " + QRCodeUrl + " with exception: " + e.getMessage());
        }

        return dashboardSettings;
    }
}
