package hu.hegpetac.music.collab.playlist.be.authentication.orchestrator;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOrchestrator {
    private final UserService userService;

    public String getAcccessTokenForUser(User user) {
        return userService.getSpotifyAccessTokenForUser(user);
    }
}
