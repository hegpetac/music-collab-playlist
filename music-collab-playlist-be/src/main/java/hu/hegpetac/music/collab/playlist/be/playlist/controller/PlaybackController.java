package hu.hegpetac.music.collab.playlist.be.playlist.controller;

import hu.hegpetac.music.collab.playlist.be.playlist.service.PlaybackService;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.HandlePlaybackApi;
import org.openapitools.model.SeekReq;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PlaybackController implements HandlePlaybackApi {
    private final PlaybackService playbackService;

    @Override
    public ResponseEntity<Void> pause() {
        playbackService.pause();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> resume() {
        playbackService.resume();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> seek(SeekReq seekReq) {
        playbackService.seek(seekReq.getPositionMs());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> skip() {
        playbackService.skip();
        return ResponseEntity.ok().build();
    }
}
