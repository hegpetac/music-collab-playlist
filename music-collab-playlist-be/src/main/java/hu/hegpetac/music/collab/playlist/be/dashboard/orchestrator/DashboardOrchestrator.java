package hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardOrchestrator {
    private final DashboardService dashboardService;

    public User getPlaylistOwner(String playlistName, int deviceCode) {
        return dashboardService.getPlaylistOwner(playlistName, deviceCode);
    }

    public boolean doesPlaylistExist(String playlistName, int deviceCode) {
        return dashboardService.doesPlaylistExist(playlistName, deviceCode);
    }
}
