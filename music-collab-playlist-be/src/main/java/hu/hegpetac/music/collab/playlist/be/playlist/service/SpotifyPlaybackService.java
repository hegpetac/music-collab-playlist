package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.authentication.service.SpotifyTokenRefreshService;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyPlaybackService {
    private final SpotifyTokenRefreshService spotifyTokenRefreshService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.spotify.com/v1/me/player")
            .build();

    public void resumePlayback(String providerId, long positonMs) throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());
                OAuth2AuthorizedClient client = spotifyTokenRefreshService.refreshIfNeeded(sessionUser.getSpotifyPrincipalId());

                if (client == null || client.getAccessToken() == null) {
                    System.err.println("No Spotify tokens found for principal: " + sessionUser.getSpotifyPrincipalId());
                }

                System.out.println("Found Spotify token: " + client.getAccessToken().getTokenValue());

                Map<String, Object> body = Map.of(
                        "uris", List.of("spotify:track:" + providerId),
                        "position_ms", positonMs
                );

                webClient.put()
                        .uri("/play")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                return;
            }
        }

        throw new UnauthorizedException("Not found logged in user");
    }

    public void pausePlayback() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());
                OAuth2AuthorizedClient client = spotifyTokenRefreshService.refreshIfNeeded(sessionUser.getSpotifyPrincipalId());

                if (client == null || client.getAccessToken() == null) {
                    System.err.println("No Spotify tokens found for principal: " + sessionUser.getSpotifyPrincipalId());
                }

                System.out.println("Found Spotify token: " + client.getAccessToken().getTokenValue());

                webClient.put()
                        .uri("/pause")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue())
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 403,
                                response -> Mono.empty()
                        )
                        .toBodilessEntity()
                        .block();

                return;
            }
        }

        throw new UnauthorizedException("Not found logged in user");
    }
}
