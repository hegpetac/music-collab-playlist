package hu.hegpetac.music.collab.playlist.be.dashboard.orchestrator;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.dashboard.model.ModifyNameDetails;
import hu.hegpetac.music.collab.playlist.be.dashboard.service.DashboardService;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.QueueRegistry;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.SuggestionRegistry;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.ModifyNameReq;
import org.openapitools.model.ModifyNameResp;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardOrchestrator {
    private final DashboardService dashboardService;
    private final SuggestionRegistry suggestionRegistry;
    private final QueueRegistry queueRegistry;

    public User getPlaylistOwner(String playlistName, int deviceCode) {
        return dashboardService.getPlaylistOwner(playlistName, deviceCode);
    }

    public boolean doesPlaylistExist(String playlistName, int deviceCode) {
        return dashboardService.doesPlaylistExist(playlistName, deviceCode);
    }

    public ModifyNameResp modifyName(ModifyNameReq modifyNameReq) {
        ModifyNameDetails details = dashboardService.modifyName(modifyNameReq);
        suggestionRegistry.renamePlaylist(details.oldName(), details.resp().getName());
        queueRegistry.renamePlaylist(details.oldName(), details.resp().getName());
        return details.resp();
    }
}
