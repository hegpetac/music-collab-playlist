package hu.hegpetac.music.collab.playlist.be.playlist.registry;


import org.openapitools.model.ProviderIdListInner;
import org.openapitools.model.TrackSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QueueRegistry extends TrackRegistry {

    public void reorderQueue(String playlistName ,List<ProviderIdListInner> providerInfo) {
        List<TrackSummary> tracks = playlists.get(playlistName);
        synchronized (tracks) {
            List<TrackSummary> newTracks = new ArrayList<>();
            for (ProviderIdListInner providerId : providerInfo) {
               newTracks.addAll(
                       tracks.stream()
                               .filter(trackSummary ->  trackSummary.getProviderId().equals(providerId.getProviderId()))
                               .toList()
               );
            }
            playlists.put(playlistName, newTracks);
        }
    }
}
