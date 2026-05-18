package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator.DashboardOrchestrator;
import hu.hegpetac.music.collab.playlist.be.dashboard.service.DashboardService;
import hu.hegpetac.music.collab.playlist.be.exception.BadRequestException;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.TrackRegistry;
import hu.hegpetac.music.collab.playlist.be.playlist.repository.PlaybackStatsRepository;
import hu.hegpetac.music.collab.playlist.be.playlist.repository.TrackStatsRepository;
import org.openapitools.model.SuggestTrackReq;
import org.openapitools.model.TrackSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {

    private final TrackRegistry registry;
    private final DashboardOrchestrator dashboardOrchestrator;
    private final ModelUpdateNotifier notifier;
    private final PlaybackStatsRepository playbackStatsRepository;
    private final TrackStatsRepository trackStatsRepository;
    private final DashboardService dashboardService;

    public SuggestionService(
            @Qualifier("trackRegistry")TrackRegistry registry,
            DashboardOrchestrator dashboardOrchestrator,
            ModelUpdateNotifier notifier,
            PlaybackStatsRepository playbackStatsRepository,
            TrackStatsRepository trackStatsRepository,
            DashboardService dashboardService
    ) {
        this.registry = registry;
        this.dashboardOrchestrator = dashboardOrchestrator;
        this.notifier = notifier;
        this.playbackStatsRepository = playbackStatsRepository;
        this.trackStatsRepository = trackStatsRepository;
        this.dashboardService = dashboardService;
    }

    public List<TrackSummary> handleSuggestion(SuggestTrackReq suggestTrackReq) throws BadRequestException {
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

        if(exists || trackRecentlyPlayed(suggestTrackReq)) {
            throw new BadRequestException("Track already suggested or recently played for playlist " + suggestTrackReq.getPlaylistName());
        }

        registry.addTrack(suggestTrackReq.getPlaylistName(), suggestTrackReq.getTrack());
        List<TrackSummary> updatedSuggestions = registry.findTrackList(suggestTrackReq.getPlaylistName()).get();

        notifier.notifySuggestionsUpdated(suggestTrackReq.getPlaylistName(), updatedSuggestions);
        return updatedSuggestions;
    }

    private boolean trackRecentlyPlayed(SuggestTrackReq suggestTrackReq) throws NotFoundException {
        var dashboard = dashboardService.getPlaylistByName(suggestTrackReq.getPlaylistName());
        var playbackStatsOpt = playbackStatsRepository.findByOwner(dashboard.getUser());

        if (playbackStatsOpt.isEmpty()) {
            throw new NotFoundException("No suggestions found for " + suggestTrackReq.getPlaylistName());
        }

        var playbackStats = playbackStatsOpt.get();
        if (dashboard.getReplayTimeLimit() == 0) {
            return false;
        }

        return playbackStats.getTrackStats().stream()
                .filter(trackStats -> trackStats.getProviderId().equals(suggestTrackReq.getTrack().getProviderId()))
                .findFirst()
                .map(track -> ChronoUnit.MINUTES.between(track.getLastPlayedAt(), Instant.now()) < dashboard.getReplayTimeLimit())
                .orElse(false);

    }
}
