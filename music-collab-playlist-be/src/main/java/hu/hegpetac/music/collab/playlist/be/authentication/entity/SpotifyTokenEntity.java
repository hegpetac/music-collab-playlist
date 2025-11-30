package hu.hegpetac.music.collab.playlist.be.authentication.entity;

import java.time.Instant;

public record SpotifyTokenEntity(
        String userId,
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        String scopes
) {}