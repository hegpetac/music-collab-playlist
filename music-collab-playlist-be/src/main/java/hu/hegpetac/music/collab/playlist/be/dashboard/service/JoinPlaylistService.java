package hu.hegpetac.music.collab.playlist.be.dashboard.service;

import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.dashboard.repository.DashboardSettingsRepository;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.JoinPlaylistReq;
import org.openapitools.model.JoinPlaylistResp;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JoinPlaylistService {
    private final DashboardSettingsRepository dashboardSettingsRepository;

    public JoinPlaylistResp joinPlaylist(JoinPlaylistReq req) throws NotFoundException {
        Optional<DashboardSettings> existingDashboardSettings = dashboardSettingsRepository.findByNameAndDeviceCode(req.getName(), req.getDeviceCode());
        if(existingDashboardSettings.isEmpty()) {
            String errorMsg = "Dashboard settings with provided name " + req.getName() + " and device code " + req.getDeviceCode() + " not found";
            System.out.println(errorMsg);
            throw new NotFoundException(errorMsg);
        }

        DashboardSettings dashboardSettings = existingDashboardSettings.get();
        return new JoinPlaylistResp(dashboardSettings.getName(), dashboardSettings.getDeviceCode());
    }
}
