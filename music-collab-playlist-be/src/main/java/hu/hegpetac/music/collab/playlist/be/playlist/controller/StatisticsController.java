package hu.hegpetac.music.collab.playlist.be.playlist.controller;

import hu.hegpetac.music.collab.playlist.be.playlist.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.StatisticsApi;
import org.openapitools.model.PlaylistStatisticsResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StatisticsController implements StatisticsApi {
    private final StatisticsService statisticsService;

    @Override
    public ResponseEntity<PlaylistStatisticsResp> getPlaylistStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

}
