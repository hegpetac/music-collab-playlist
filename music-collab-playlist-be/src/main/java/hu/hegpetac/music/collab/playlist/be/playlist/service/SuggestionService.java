package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator.DashboardOrchestrator;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.TrackRegistry;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.SuggestTrackReq;
import org.openapitools.model.TrackSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    private final TrackRegistry registry;
    private final DashboardOrchestrator dashboardOrchestrator;
    private final ModelUpdateNotifier notifier;

    public SuggestionService(@Qualifier("trackRegistry")TrackRegistry registry, DashboardOrchestrator dashboardOrchestrator, ModelUpdateNotifier notifier) {
        this.registry = registry;
        this.dashboardOrchestrator = dashboardOrchestrator;
        this.notifier = notifier;
    }

    public List<TrackSummary> handleSuggestion(SuggestTrackReq suggestTrackReq) {
        if (!dashboardOrchestrator.doesPlaylistExist(suggestTrackReq.getPlaylistName(), suggestTrackReq.getDeviceCode())) {
            throw new NotFoundException("Playlist with following parameters doesn't exist: " + suggestTrackReq.getPlaylistName() + " " + suggestTrackReq.getDeviceCode());
        }
        registry.registerPlaylist(suggestTrackReq.getPlaylistName());
        var suggestionsOpt = registry.findTrackList(suggestTrackReq.getPlaylistName());

        if(suggestionsOpt.isEmpty()) {
            throw new NotFoundException("No suggestions found for " + suggestTrackReq.getPlaylistName());
        }

        var suggestions = suggestionsOpt.get();
        boolean exists = suggestions.stream()
                .anyMatch(t -> t.getProviderId().equals(suggestTrackReq.getTrack().getProviderId()));

        if(exists) {
            return suggestions;
        }

        registry.addTrack(suggestTrackReq.getPlaylistName(), suggestTrackReq.getTrack());
        List<TrackSummary> updatedSuggestions = registry.findTrackList(suggestTrackReq.getPlaylistName()).get();

        notifier.notifySuggestionsUpdated(suggestTrackReq.getPlaylistName(), updatedSuggestions);
        return updatedSuggestions;
    }
}
