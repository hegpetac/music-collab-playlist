package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("playlistOwnershipService")
@RequiredArgsConstructor
public class PlaylistOwnershipService {

    private final PlaylistRepository playlistRepository;

    public boolean isOwner(Long playlistId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        String principalUserId = (String) authentication.getPrincipal();
        return playlistRepository.findById(playlistId)
                .map(pl -> pl.getOwner().getId().toString().equals(principalUserId))
                .orElse(false);
    }
}
