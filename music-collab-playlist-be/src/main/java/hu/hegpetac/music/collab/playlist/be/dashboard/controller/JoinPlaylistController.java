package hu.hegpetac.music.collab.playlist.be.dashboard.controller;

import hu.hegpetac.music.collab.playlist.be.dashboard.service.JoinPlaylistService;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.JoinApi;
import org.openapitools.model.JoinPlaylistReq;
import org.openapitools.model.JoinPlaylistResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class JoinPlaylistController implements JoinApi {
    private final JoinPlaylistService joinPlaylistService;

    @Override
    public ResponseEntity<JoinPlaylistResp> joinPlaylist(JoinPlaylistReq joinPlaylistReq) {
        return ResponseEntity.ok(joinPlaylistService.joinPlaylist(joinPlaylistReq));
    }
}
