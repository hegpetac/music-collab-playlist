package hu.hegpetac.music.collab.playlist.be.playlist.registry;


import org.openapitools.model.ProviderIdListInner;
import org.openapitools.model.TrackSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueueRegistry {
    private final ConcurrentHashMap<String, List<TrackSummary>> playlists = new ConcurrentHashMap<>();

    public void registerPlaylist(String playlistName) {
        playlists.putIfAbsent(playlistName, Collections.synchronizedList(new ArrayList<>()));
    }

    public Optional<List<TrackSummary>> findQueue(String playlistName) {
        return Optional.ofNullable(playlists.get(playlistName));
    }

    public void addTrack(String playlistName, TrackSummary track) {
        playlists.computeIfAbsent(playlistName, k -> Collections.synchronizedList(new ArrayList<>())).add(track);
    }

    public void renamePlaylist(String oldName, String newName) {
        var existing = playlists.remove(oldName);
        if (existing == null) {
            registerPlaylist(newName);
            return;
        }
        playlists.put(newName, existing);
    }

    public boolean deleteTrack(String playlistName, String providerId) {
        List<TrackSummary> tracks = playlists.get(playlistName);
        if (tracks == null) {
            return false;
        }

        synchronized (tracks) {
            return tracks.removeIf(track -> track.getProviderId().equals(providerId));
        }
    }

    public boolean clearPlaylist(String playlistName) {
        List<TrackSummary> tracks = playlists.get(playlistName);
        if (tracks == null) {
            return false;
        }

        synchronized (tracks) {
            tracks.clear();
        }

        return true;
    }

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
