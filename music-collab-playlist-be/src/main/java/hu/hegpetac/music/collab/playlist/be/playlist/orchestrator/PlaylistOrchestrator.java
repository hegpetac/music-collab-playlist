package hu.hegpetac.music.collab.playlist.be.playlist.orchestrator;

import hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator.DashboardOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistOrchestrator {
    private final DashboardOrchestrator dashboardOrchestrator;

    public boolean doesPlaylistExist(String playlistName, int deviceCode) {
        return dashboardOrchestrator.doesPlaylistExist(playlistName, deviceCode);
    }
}
