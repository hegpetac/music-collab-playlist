package hu.hegpetac.music.collab.playlist.be.authentication.repository;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.SpotifyTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class SpotifyTokenRepository {

    private final JdbcTemplate jdbc;

    public SpotifyTokenEntity findByUserId (String userId) {
        return jdbc.query(
                "SELECT * FROM user_spotify_tokens WHERE user_id = ?",
                rs -> rs.next()
                        ? new SpotifyTokenEntity(
                        rs.getString("user_id"),
                        rs.getString("access_token"),
                        rs.getString("refresh_token"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("issued_at").toInstant(),
                        rs.getString("scopes")
                )
                        : null,
                userId
        );
    }

    public void save(SpotifyTokenEntity e) {
        jdbc.update("""
            INSERT INTO user_spotify_tokens
                (user_id, access_token, refresh_token, issued_at, expires_at, scopes)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET
                access_token = EXCLUDED.access_token,
                refresh_token = EXCLUDED.refresh_token,
                issued_at = EXCLUDED.issued_at,
                expires_at = EXCLUDED.expires_at,
                scopes = EXCLUDED.scopes
        """, e.userId(), e.accessToken(), e.refreshToken(), Timestamp.from(e.issuedAt()), Timestamp.from(e.expiresAt()), e.scopes());
    }

    public void delete(String userId) {
        jdbc.update("DELETE FROM user_spotify_tokens WHERE user_id = ?", userId);
    }
}
