package hu.hegpetac.music.collab.playlist.be.playlist.model;

import lombok.Data;
import org.openapitools.model.PlaybackState;
import org.openapitools.model.PlaybackStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

@Data
public class PlaybackSession {
    private Duration totalDuration;
    private Instant startedAt;
    private Instant resumedAt;
    private Duration playedDuration = Duration.ZERO;
    private PlaybackState playbackState;
    private ScheduledFuture<?> finishFuture;

    public Duration getCurrentPosition() {
        if (playbackState.getStatus() == PlaybackStatus.PLAYING) {
            return playedDuration.plus(Duration.between(resumedAt, Instant.now()));
        }
        return playedDuration;
    }

    public Duration getRemaining() {
        return totalDuration.minus(getCurrentPosition());
    }
}
