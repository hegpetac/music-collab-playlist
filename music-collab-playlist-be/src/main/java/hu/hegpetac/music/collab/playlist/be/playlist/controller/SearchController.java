package hu.hegpetac.music.collab.playlist.be.playlist.controller;

import hu.hegpetac.music.collab.playlist.be.exception.BadRequestException;
import hu.hegpetac.music.collab.playlist.be.playlist.service.SpotifySearchService;
import hu.hegpetac.music.collab.playlist.be.playlist.service.YoutubeSearchService;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.SearchApi;
import org.openapitools.model.OwnerSearchReq;
import org.openapitools.model.SearchReq;
import org.openapitools.model.TrackSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController implements SearchApi {
    private final SpotifySearchService spotifySearchService;
    private final YoutubeSearchService youtubeSearchService;

    @Override
    public ResponseEntity<List<TrackSummary>> searchMusic(SearchReq searchReq) {
        System.out.println("Search request for provider "  + searchReq.getProvider() + " with query: " + searchReq.getQuery());
        switch (searchReq.getProvider()) {
            case SPOTIFY -> {
                return ResponseEntity.ok(spotifySearchService.searchMusic(searchReq));
            }
            case YOUTUBE -> {
                return ResponseEntity.ok(youtubeSearchService.searchMusic(searchReq));
            }
            default -> throw new BadRequestException("Unknown provider: " + searchReq.getProvider());
        }
    }

    @Override
    public ResponseEntity<List<TrackSummary>> searchAsOwner(OwnerSearchReq ownerSearchReq) {
        switch (ownerSearchReq.getProvider()) {
            case SPOTIFY -> {
                return ResponseEntity.ok(spotifySearchService.searchAsOwner(ownerSearchReq.getQuery()));
            }
            case YOUTUBE -> {
                return ResponseEntity.ok(youtubeSearchService.searchAsOwner(ownerSearchReq.getQuery()));
            }
            default -> throw new BadRequestException("Unknown provider: " + ownerSearchReq.getProvider());
        }
    }
}
