package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.playlist.orchestrator.PlaylistOrchestrator;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.SearchReq;
import org.openapitools.model.TrackSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifySearchService {
    private final PlaylistOrchestrator playlistOrchestrator;


    public List<TrackSummary> searchMusic(SearchReq searchReq) {
        String accessToken = playlistOrchestrator.getSpotifyAccessToken(searchReq.getPlaylistName(), searchReq.getDeviceCode());
        return null;
    }

}
