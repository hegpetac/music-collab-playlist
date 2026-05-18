package hu.hegpetac.music.collab.playlist.be.playlist.entity;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table
public class PlaybackStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    @Transient
    private int totalTracksPlayed;
    @Transient
    private int totalUniqueTracksPlayed;
    @Transient
    private int totalSecondsPlayed;

    @OneToMany(mappedBy = "playbackStats", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackStats> trackStats;

    public void addNewTrackStats(TrackStats t) {
        trackStats.add(t);
        t.setPlaybackStats(this);
    }
}
