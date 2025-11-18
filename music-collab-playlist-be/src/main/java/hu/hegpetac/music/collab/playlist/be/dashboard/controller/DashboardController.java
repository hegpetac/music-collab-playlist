package hu.hegpetac.music.collab.playlist.be.dashboard.controller;

import hu.hegpetac.music.collab.playlist.be.dashboard.service.DashboardService;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.DashboardApi;
import org.openapitools.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class DashboardController implements org.openapitools.api.DashboardApi {
    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<DashboardSettings> getDashboardSettings() {
        return ResponseEntity.ok(dashboardService.getDashboardSettings());
    }

    @Override
    public ResponseEntity<RegenerateCodeResp> regenerateDeviceCode() {
        return  ResponseEntity.ok(dashboardService.regenerateDeviceCode());
    }

    @Override
    public ResponseEntity<SuggestionPlaybackMode> setSuggestionPlayback(ModifySuggestionPlaybackModeReq modifySuggestionPlaybackModeReq) {
        return ResponseEntity.ok(dashboardService.modifySuggestionPlaybackMode(modifySuggestionPlaybackModeReq));
    }

    @Override
    public ResponseEntity<YoutubePlaybackMode> setYoutubePlayback(ModifyYoutubePlaybackModeReq modifyYoutubePlaybackModeReq) {
        return ResponseEntity.ok(dashboardService.modifyYoutubePlaybackMode(modifyYoutubePlaybackModeReq));
    }

    @Override
    public ResponseEntity<String> setName(ModifyNameReq modifyNameReq) {
        return ResponseEntity.ok(dashboardService.modifyName(modifyNameReq));
    }
}
