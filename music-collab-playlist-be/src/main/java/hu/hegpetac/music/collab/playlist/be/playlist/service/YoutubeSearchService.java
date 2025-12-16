package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import hu.hegpetac.music.collab.playlist.be.playlist.orchestrator.PlaylistOrchestrator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.Provider;
import org.openapitools.model.SearchReq;
import org.openapitools.model.TrackSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeSearchService {
    private final PlaylistOrchestrator playlistOrchestrator;

    @Value("${YOUTUBE_TOKEN}")
    private String youtubeApiKey;

    private WebClient client;

    @PostConstruct
    void init() {
        client = WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .build();
    }

    public List<TrackSummary> searchAsOwner(String query) throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Not found logged in user");
        }

        return searchYouTubeAPI(query);
    }

    public List<TrackSummary> searchMusic(SearchReq searchReq) throws NotFoundException{
        if (!playlistOrchestrator.doesPlaylistExist(searchReq.getPlaylistName(), searchReq.getDeviceCode())) {
            throw new NotFoundException("Playlist " + searchReq.getPlaylistName() + " does not exist");
        }

        return searchYouTubeAPI(searchReq.getQuery());
    }

    private List<TrackSummary> searchYouTubeAPI(String query) {
        Map<String, Map<String, String>> results = client.get()
                .uri(uri -> uri.path("/search")
                        .queryParam("key", youtubeApiKey)
                        .queryParam("part", "snippet")
                        .queryParam("type", "video")
                        .queryParam("maxResults", 5)
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (List<Map<String, Object>>) body.get("items"))
                .map(items -> {
                    Map<String, Map<String, String>> map = new HashMap<>();
                    for (var item : items) {
                        Map<String, Object> id = (Map<String, Object>) item.get("id");
                        Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");

                        String videoId = (String) id.get("videoId");

                        map.put(videoId, Map.of(
                                "title", (String) snippet.get("title"),
                                "artist", (String) snippet.get("channelTitle"),
                                "thumbnail", ((Map<String, Object>) snippet.get("thumbnails")).get("default").toString()
                        ));
                    }
                    return map;
                })
                .block();

        if (results == null || results.isEmpty()) return List.of();

        String joinedIds = String.join(",", results.keySet());

        Map<String, Integer> durations = client.get()
                .uri(uri -> uri.path("/videos")
                        .queryParam("key", youtubeApiKey)
                        .queryParam("part", "contentDetails")
                        .queryParam("id", joinedIds)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (List<Map<String, Object>>) body.get("items"))
                .map(items -> {
                    Map<String, Integer> map = new HashMap<>();
                    for (var item : items) {
                        String id = (String) item.get("id");
                        Map<String, Object> details = (Map<String, Object>) item.get("contentDetails");
                        String iso = (String) details.get("duration");
                        map.put(id, isoToSeconds(iso));
                    }
                    return map;
                })
                .block();

        return results.entrySet().stream()
                .map(e -> {
                    TrackSummary dto = new TrackSummary();
                    dto.setProvider(Provider.YOUTUBE);
                    dto.setProviderId(e.getKey());
                    dto.setTitle(e.getValue().get("title"));
                    dto.setArtist(e.getValue().get("artist"));
                    dto.setThumbnail(extractUrl(e.getValue().get("thumbnail")));
                    dto.setDurationMs(durations.getOrDefault(e.getKey(), 0) * 1000);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private int isoToSeconds(String iso) {
        int minutes = 0, seconds = 0;
        var m = java.util.regex.Pattern.compile("PT(?:(\\d+)M)?(?:(\\d+)S)?").matcher(iso);
        if (m.matches()) {
            if (m.group(1) != null) minutes = Integer.parseInt(m.group(1));
            if (m.group(2) != null) seconds = Integer.parseInt(m.group(2));
        }
        return minutes * 60 + seconds;
    }

    private String extractUrl(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Matcher matcher = Pattern.compile("(?<=url=)(.*?)(?=,|})").matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }
}
