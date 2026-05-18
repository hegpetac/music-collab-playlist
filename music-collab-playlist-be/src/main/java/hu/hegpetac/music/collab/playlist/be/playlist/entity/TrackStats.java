package hu.hegpetac.music.collab.playlist.be.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "track_stats")
public class TrackStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    @Column(name = "track_length")
    private int trackLengthSeconds;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "timesPlayed")
    private int timesPlayed;

    @Column(name = "title")
    private String title;

    @Column(name = "artist")
    private String artist;

    @Column(name = "last_played_at")
    private Instant lastPlayedAt;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "playback_stats_id")
    private PlaybackStats playbackStats;

    public void incrementTimesPlayed() {
        timesPlayed ++;
        lastPlayedAt = Instant.now();
    }
}
