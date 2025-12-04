package hu.hegpetac.music.collab.playlist.be.playlist.controller;

import hu.hegpetac.music.collab.playlist.be.playlist.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.SuggestApi;
import org.openapitools.model.SuggestTrackReq;
import org.openapitools.model.TrackSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class SuggestionController implements SuggestApi {
    private final SuggestionService suggestionService;

    @Override
    public ResponseEntity<List<TrackSummary>> suggestTrack(SuggestTrackReq suggestTrackReq) {
        return ResponseEntity.ok(suggestionService.handleSuggestion(suggestTrackReq));
    }
}
