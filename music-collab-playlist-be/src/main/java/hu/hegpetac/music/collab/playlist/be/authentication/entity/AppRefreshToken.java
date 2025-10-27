package hu.hegpetac.music.collab.playlist.be.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "app_refresh_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRefreshToken {

    @Id
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "issued_at")
    private Instant issuedAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;
}
