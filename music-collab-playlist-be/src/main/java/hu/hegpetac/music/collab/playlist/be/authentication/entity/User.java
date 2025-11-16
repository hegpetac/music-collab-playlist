package hu.hegpetac.music.collab.playlist.be.authentication.entity;

import hu.hegpetac.music.collab.playlist.be.playlist.entity.Playlist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    private boolean enabled = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_account_id", referencedColumnName = "id")
    private OAuthAccount spotifyAccount;

    @OneToOne(mappedBy = "owner")
    private Playlist playlist;
}