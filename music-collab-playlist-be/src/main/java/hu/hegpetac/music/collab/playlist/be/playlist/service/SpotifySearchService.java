package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.service.SpotifyTokenRefreshService;
import hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator.DashboardOrchestrator;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.Provider;
import org.openapitools.model.SearchReq;
import org.openapitools.model.TrackSummary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifySearchService {
    private final DashboardOrchestrator dashboardOrchestrator;
    private final SpotifyTokenRefreshService spotifyTokenRefreshService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.spotify.com/v1")
            .build();

    public List<TrackSummary> searchMusic(SearchReq searchReq) {
        String spotifyPrincipalId = dashboardOrchestrator.getPlaylistOwner(searchReq.getPlaylistName(), searchReq.getDeviceCode()).getSpotifyPrincipalId();
        OAuth2AuthorizedClient client = spotifyTokenRefreshService.refreshIfNeeded(spotifyPrincipalId);

        if (client == null || client.getAccessToken() == null) {
            System.err.println("No Spotify tokens found for principal: " + spotifyPrincipalId);
            return List.of();
        }

        System.out.println("Found Spotify token: " + client.getAccessToken().getTokenValue());

        SpotifySearchResponse response = webClient.get()
                .uri(uri -> uri.path("/search")
                        .queryParam("q", searchReq.getQuery())
                        .queryParam("type", "track")
                        .queryParam("limit", 5)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue())
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .block();

        if (response == null || response.tracks == null || response.tracks.items == null) {
            return List.of();
        }

        return response.tracks.items.stream()
                .map(this::mapTrack)
                .toList();
    }

    private TrackSummary mapTrack(Item item) {
        TrackSummary dto = new TrackSummary();
        dto.setProvider(Provider.SPOTIFY);
        dto.setProviderId(item.id());
        dto.setTitle(item.name());
        dto.setArtist(item.artists() != null && !item.artists().isEmpty() ? item.artists().getFirst().name() : "Unknown");
        dto.setThumbnail(item.album() != null && item.album().images() != null && !item.album().images().isEmpty()
                ? item.album().images().getFirst().url
                : null);
        dto.setDurationMs(item.duration_ms);
        return dto;
    }

    record SpotifySearchResponse(Tracks tracks) {}
    record Tracks(List<Item> items) {}
    record Item(String id, String name, Album album, List<Artist> artists, int duration_ms) {}
    record Album(List<Image> images) {}
    record Image(String url) {}
    record Artist(String name) {}
}
