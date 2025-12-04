package hu.hegpetac.music.collab.playlist.be.playlist.service;

import lombok.RequiredArgsConstructor;
import org.openapitools.model.SuggestTrackReq;
import org.openapitools.model.TrackSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private List<TrackSummary> currentSuggestions;

    public List<TrackSummary> handleSuggestion(SuggestTrackReq suggestTrackReq) {
        System.out.println(suggestTrackReq.getTrack().getTitle());
        return List.of();
    }
}
