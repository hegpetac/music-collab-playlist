package hu.hegpetac.music.collab.playlist.be.playlist.registry;

import hu.hegpetac.music.collab.playlist.be.playlist.model.PlaybackSession;
import org.openapitools.model.PlaybackState;
import org.openapitools.model.PlaybackStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlaybackSessionRegistry {
    protected final ConcurrentHashMap<String, PlaybackSession> playbackSessions = new ConcurrentHashMap<>();

    public void registerPlaylist(String playlistName) {
        playbackSessions.putIfAbsent(playlistName, initSession());
    }

    public Optional<PlaybackSession> findPlaybackSession(String playlistName) {
        return Optional.ofNullable(playbackSessions.get(playlistName));
    }

    public void updatePlaybackSession(String playlistName, PlaybackSession playbackSession) {
        PlaybackSession playbackState = playbackSessions.get(playlistName);
        if (playbackState == null) {
            registerPlaylist(playlistName);
        }
        playbackSessions.put(playlistName, playbackSession);
    }

    public void renamePlaylist(String oldName, String newName) {
        var existing = playbackSessions.remove(oldName);
        if (existing == null) {
            registerPlaylist(newName);
            return;
        }
        playbackSessions.put(newName, existing);
    }

    private PlaybackSession initSession() {
        PlaybackState playbackState = new PlaybackState();
        playbackState.setStatus(PlaybackStatus.PAUSED);
        playbackState.setActiveTrack(null);
        playbackState.setPositionMS(BigDecimal.ZERO);

        PlaybackSession playbackSession = new PlaybackSession();
        playbackSession.setPlaybackState(playbackState);
        playbackSession.setFinishFuture(null);

        return playbackSession;
    }
}
