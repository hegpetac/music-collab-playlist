package hu.hegpetac.music.collab.playlist.be.playlist.orchestrator;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.orchestrator.UserOrchestrator;
import hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator.DashboardOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistOrchestrator {
    private final DashboardOrchestrator dashboardOrchestrator;
    private final UserOrchestrator userOrchestrator;

    public String getSpotifyAccessToken(String playlistName, int deviceCode) {
        User playlistOwner = dashboardOrchestrator.getPlaylistOwner(playlistName, deviceCode);
        return userOrchestrator.getAcccessTokenForUser(playlistOwner);
    }

    public boolean doesPlaylistExist(String playlistName, int deviceCode) {
        return dashboardOrchestrator.doesPlaylistExist(playlistName, deviceCode);
    }
}
