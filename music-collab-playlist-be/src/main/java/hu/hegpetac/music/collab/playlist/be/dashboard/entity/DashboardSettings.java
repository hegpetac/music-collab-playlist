package hu.hegpetac.music.collab.playlist.be.dashboard.entity;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboard_settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    @Min(0) @Max(24 * 60)
    private int replayTimeLimit = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "youtube_playback_mode")
    YoutubePlaybackMode youtubePlaybackMode = YoutubePlaybackMode.OPEN_NEW_TAB;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_playback_mode")
    SuggestionPlaybackMode suggestionPlaybackMode = SuggestionPlaybackMode.COLLECT_SUGGESTIONS;

    @Column(name = "device_code", unique = true)
    private Integer deviceCode;

    @OneToOne(mappedBy = "dashboardSettings")
    private User user;

    @Transient
    private String googleAccountEmail;

    @Transient
    private String spotifyAccountDisplayName;
}
