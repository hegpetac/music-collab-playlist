package hu.hegpetac.music.collab.playlist.be.playlist.controller;

import hu.hegpetac.music.collab.playlist.be.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.HandlePlaylistApi;
import org.openapitools.model.AddExistingTrackReq;
import org.openapitools.model.AddTrackReq;
import org.openapitools.model.DeleteTrackReq;
import org.openapitools.model.ProviderIdListInner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlaylistController implements HandlePlaylistApi {
    private final PlaylistService playlistService;

    @Override
    public ResponseEntity<Void> addTrack(AddTrackReq addTrackReq) {
        playlistService.addTrack(addTrackReq);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteTrack(DeleteTrackReq deleteTrackReq) {
        playlistService.deleteTrack(deleteTrackReq);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> addTrackFromList(AddExistingTrackReq addExistingTrackReq) {
        playlistService.addTrackFromList(addExistingTrackReq);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> reorderQueue(List<ProviderIdListInner> providerIdListInner) {
        playlistService.reorderQueue(providerIdListInner);
        return ResponseEntity.ok().build();
    }
}
