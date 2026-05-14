package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import hu.hegpetac.music.collab.playlist.be.playlist.model.PlaybackSession;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.PlaybackSessionRegistry;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.QueueRegistry;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.PlaybackState;
import org.openapitools.model.PlaybackStatus;
import org.openapitools.model.Provider;
import org.openapitools.model.TrackSummary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class PlaybackService {
    private final ModelUpdateNotifier notifier;
    private final QueueRegistry queueRegistry;
    private final PlaybackSessionRegistry playbackSessionRegistry;
    private final TaskScheduler scheduler;
    private final SpotifyPlaybackService spotifyPlaybackService;

    public void pause() {
        System.out.println("pausing");
        String playlistName = getPlaylistFromSession().getName();
        var sessionOpt = playbackSessionRegistry.findPlaybackSession(playlistName);

        if (sessionOpt.isEmpty()) {
            throw new NotFoundException("Playback session not found: " + playlistName);
        }

        PlaybackSession session = sessionOpt.get();

        Duration playedSinceLastResume = Duration.between(session.getResumedAt(), Instant.now());
        session.setPlayedDuration(
                session.getPlayedDuration().plus(playedSinceLastResume)
        );
        session.getFinishFuture().cancel(false);

        session.getPlaybackState().setStatus(PlaybackStatus.PAUSED);
        session.getPlaybackState().setPositionMS(BigDecimal.valueOf(session.getPlayedDuration().toMillis()));
        playbackSessionRegistry.updatePlaybackSession(playlistName, session);

        handleSessionStateChange(playlistName, session.getPlaybackState());
    }

    public void resume() {
        System.out.println("resuming");
        String playlistName = getPlaylistFromSession().getName();
        playbackSessionRegistry.registerPlaylist(playlistName);
        var sessionOpt = playbackSessionRegistry.findPlaybackSession(playlistName);

        if (sessionOpt.isEmpty()) {
            throw new NotFoundException("Playback session not found: " + playlistName);
        }

        PlaybackSession session = sessionOpt.get();
        TrackSummary activeTrack = session.getPlaybackState().getActiveTrack();

        if (activeTrack == null) {
            startNextTrack(playlistName, session);
        } else {
            session.setResumedAt(Instant.now());
            ScheduledFuture<?> future = scheduleTrackFinish(playlistName, session);
            session.setFinishFuture(future);
            session.getPlaybackState().setStatus(PlaybackStatus.PLAYING);
            session.getPlaybackState().setPositionMS(BigDecimal.valueOf(session.getCurrentPosition().toMillis()));
            playbackSessionRegistry.updatePlaybackSession(playlistName, session);

            handleSessionStateChange(playlistName, session.getPlaybackState());

        }
    }


    private void startNextTrack(String playlistName, PlaybackSession session) {
        System.out.println("Starting next playback session for " + playlistName);
        PlaybackState playbackState = session.getPlaybackState();
        var queueOpt = queueRegistry.findTrackList(playlistName);
        if (queueOpt.isEmpty()) {
            throw new NotFoundException("Queue not found: " + playlistName);
        }
        List<TrackSummary> queue = queueOpt.get();
        if (queue.isEmpty()) {
            playbackState.setActiveTrack(null);
            playbackState.setStatus(PlaybackStatus.QUEUE_EMPTY);
            playbackState.setPositionMS(BigDecimal.ZERO);
            playbackSessionRegistry.updatePlaybackSession(playlistName, session);

            handleSessionStateChange(playlistName, playbackState);
        } else {
            TrackSummary track = queue.getFirst();
            playbackState.setActiveTrack(track);
            playbackState.setStatus(PlaybackStatus.PLAYING);
            playbackState.setPositionMS(BigDecimal.ZERO);
            queueRegistry.deleteTrack(playlistName, track.getProviderId());
            notifier.notifyQueueUpdated(playlistName, queueRegistry.findTrackList(playlistName).get());
            session.setTotalDuration(Duration.ofMillis(track.getDurationMs()));
            session.setStartedAt(Instant.now());
            session.setResumedAt(Instant.now());
            session.setPlayedDuration(Duration.ZERO);
            ScheduledFuture<?> future = scheduleTrackFinish(playlistName, session);
            session.setFinishFuture(future);
            playbackSessionRegistry.updatePlaybackSession(playlistName, session);

            handleSessionStateChange(playlistName, playbackState);
        }
    }


    public void skip() {
        System.out.println("Skipping");
        String playlistName = getPlaylistFromSession().getName();
        var sessionOpt = playbackSessionRegistry.findPlaybackSession(playlistName);

        if (sessionOpt.isEmpty()) {
            throw new NotFoundException("Playback session not found: " + playlistName);
        }

        PlaybackSession session = sessionOpt.get();
        startNextTrack(playlistName, session);
    }

    public void seek(BigDecimal position) {
        System.out.println("Seeking");
        String playlistName = getPlaylistFromSession().getName();
        var sessionOpt = playbackSessionRegistry.findPlaybackSession(playlistName);

        if (sessionOpt.isEmpty()) {
            throw new NotFoundException("Playback session not found: " + playlistName);
        }

        PlaybackSession session = sessionOpt.get();
        PlaybackState state = session.getPlaybackState();
        session.setPlayedDuration(Duration.ofMillis(position.longValue()));
        session.setResumedAt(Instant.now());
        ScheduledFuture<?> future = scheduleTrackFinish(playlistName, session);
        session.setFinishFuture(future);
        state.setPositionMS(position);
        state.setStatus(PlaybackStatus.PLAYING);

        playbackSessionRegistry.updatePlaybackSession(playlistName, session);

        handleSessionStateChange(playlistName, state);
    }

    private void handleSessionStateChange(String playlistName, PlaybackState state) {
        System.out.println("Playback state changed: " + playlistName + ", " + state.getStatus().name());
        spotifyPlaybackService.pausePlayback();

        if (state.getStatus() == PlaybackStatus.PLAYING && state.getActiveTrack().getProvider() == Provider.SPOTIFY) {
            spotifyPlaybackService.resumePlayback(state.getActiveTrack().getProviderId(), state.getPositionMS().longValue());
        }
        notifier.notifyPlaybackEvent(playlistName, state);
    }

    private ScheduledFuture<?> scheduleTrackFinish(String playlistName, PlaybackSession session) {
        Instant finishTime = Instant.now().plus(session.getRemaining());
        System.out.println("Playback will finish: " + session.getRemaining());

        return scheduler.schedule(() -> finishTrack(playlistName, session), finishTime);
    }

    private void finishTrack(String playlistName, PlaybackSession session) {
        startNextTrack(playlistName, session);
    }

    private DashboardSettings getPlaylistFromSession() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());
                return sessionUser.getDashboardSettings();
            }
        }

        throw new UnauthorizedException("No authenticated user found in session");
    }
}
