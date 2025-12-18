package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.QueueRegistry;
import hu.hegpetac.music.collab.playlist.be.playlist.registry.SuggestionRegistry;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final ModelUpdateNotifier notifier;
    private final SuggestionRegistry suggestionRegistry;
    private final QueueRegistry queueRegistry;

    public void addTrack(AddTrackReq addTrackReq) {
        String playlistName = getPlaylistFromSession().getName();
        queueRegistry.registerPlaylist(playlistName);
        var queueOpt = queueRegistry.findQueue(playlistName);

        if (queueOpt.isEmpty()) {
            throw new NotFoundException("Queue not found: " + playlistName);
        }

        queueRegistry.addTrack(playlistName, addTrackReq.getTrack());
        List<TrackSummary> updatedQueue = queueRegistry.findQueue(playlistName).get();

        notifier.notifyQueueUpdated(playlistName, updatedQueue);
    }

    public void deleteSuggestion(DeleteTrackReq deleteTrackReq) {
        String playlistName = getPlaylistFromSession().getName();
        List<TrackSummary> suggestions = suggestionRegistry.findSuggestions(playlistName).get();
        if (suggestions.stream().anyMatch(r -> r.getProviderId().equals(deleteTrackReq.getProviderId()) && r.getProvider().equals(deleteTrackReq.getProvider()))) {
            suggestionRegistry.deleteTrack(playlistName, deleteTrackReq.getProviderId());
            List<TrackSummary> newSuggestions = suggestionRegistry.findSuggestions(playlistName).get();
            notifier.notifySuggestionsUpdated(playlistName, newSuggestions);
        }

        throw new NotFoundException("Track with providerId" + deleteTrackReq.getProviderId() + "not found in playlist: " + playlistName);
    }

    public void addSuggestedTrack(AddExistingTrackReq addExistingTrackReq) {
        String playlistName = getPlaylistFromSession().getName();
        TrackSummary track = addExistingTrackReq.getTrack();

        suggestionRegistry.deleteTrack(playlistName, track.getProviderId());
        queueRegistry.addTrack(playlistName, track);
        queueRegistry.reorderQueue(playlistName, addExistingTrackReq.getOrder());

        List<TrackSummary> updatedQueue = queueRegistry.findQueue(playlistName).get();
        List<TrackSummary> updatedSuggestions = suggestionRegistry.findSuggestions(playlistName).get();
        notifier.notifyQueueUpdated(playlistName, updatedQueue);
        notifier.notifySuggestionsUpdated(playlistName, updatedSuggestions);
    }

    public void addRecommendedTrack(AddExistingTrackReq addExistingTrackReq) {
        String playlistName = getPlaylistFromSession().getName();
        TrackSummary track = addExistingTrackReq.getTrack();

        queueRegistry.addTrack(playlistName, track);
        queueRegistry.reorderQueue(playlistName, addExistingTrackReq.getOrder());

        List<TrackSummary> updatedQueue = queueRegistry.findQueue(playlistName).get();
        notifier.notifyQueueUpdated(playlistName, updatedQueue);
    }

    public void reorderQueue(List<ProviderIdListInner> ids) {
        String playlistName = getPlaylistFromSession().getName();

        queueRegistry.reorderQueue(playlistName, ids);

        List<TrackSummary> updatedQueue = queueRegistry.findQueue(playlistName).get();
        notifier.notifyQueueUpdated(playlistName, updatedQueue);
    }

    private DashboardSettings getPlaylistFromSession() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());
                return sessionUser.getDashboardSettings();
            }
        }

        throw new UnauthorizedException("No authenticated user found in session");
    }
}
