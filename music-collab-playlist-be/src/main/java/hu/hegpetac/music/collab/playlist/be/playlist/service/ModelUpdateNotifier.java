package hu.hegpetac.music.collab.playlist.be.playlist.service;

import lombok.RequiredArgsConstructor;
import org.openapitools.model.PlaybackState;
import org.openapitools.model.TrackSummary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelUpdateNotifier {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifySuggestionsUpdated(String playlistName, List<TrackSummary> newList) {
        messagingTemplate.convertAndSend("/topic/suggestions/" + playlistName, newList);
    }

    public void notifyQueueUpdated(String playlistName, List<TrackSummary> queue) {
        messagingTemplate.convertAndSend("/topic/queue/" + playlistName, queue);
    }

    public void notifyPlaybackEvent(String playlistName, PlaybackState state) {
        messagingTemplate.convertAndSend("/topic/playback/" + playlistName, state);
    }
}
