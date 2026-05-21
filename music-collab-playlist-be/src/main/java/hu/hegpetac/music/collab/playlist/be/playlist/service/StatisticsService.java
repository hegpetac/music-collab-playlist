package hu.hegpetac.music.collab.playlist.be.playlist.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.exception.NotFoundException;
import hu.hegpetac.music.collab.playlist.be.exception.UnauthorizedException;
import hu.hegpetac.music.collab.playlist.be.playlist.entity.PlaybackStats;
import hu.hegpetac.music.collab.playlist.be.playlist.entity.Provider;
import hu.hegpetac.music.collab.playlist.be.playlist.entity.TrackStats;
import hu.hegpetac.music.collab.playlist.be.playlist.mapper.TrackMapper;
import hu.hegpetac.music.collab.playlist.be.playlist.repository.PlaybackStatsRepository;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.PlaylistStatistics;
import org.openapitools.model.PlaylistStatisticsResp;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final PlaybackStatsRepository playbackStatsRepository;
    private final TrackMapper trackMapper;

    public PlaylistStatisticsResp getStatistics() {
        System.out.println("statistics");

        var trackStatistics = getPlaybackStatsFromSession().getTrackStats();

        var stats = new PlaylistStatistics();
        stats.setNumberOfUniqueSongsPlayed(trackStatistics.size());
        stats.setTotalMinutesPlayed(
                trackStatistics.stream()
                    .map(trackStats -> trackStats.getTimesPlayed() * (trackStats.getTrackLengthSeconds() / 1000))
                    .reduce(0, Integer::sum) / 60
        );
        stats.setYoutubeMinutesPlayed(
                trackStatistics.stream()
                    .filter(trackStats -> Provider.YOUTUBE == trackStats.getProvider())
                    .map(trackStats -> trackStats.getTimesPlayed() * (trackStats.getTrackLengthSeconds() / 1000))
                    .reduce(0, Integer::sum) / 60
        );
        stats.setSpotifyMinutesPlayed(
                trackStatistics.stream()
                        .filter(trackStats -> Provider.SPOTIFY == trackStats.getProvider())
                        .map(trackStats -> trackStats.getTimesPlayed() * (trackStats.getTrackLengthSeconds() / 1000))
                        .reduce(0, Integer::sum) / 60
        );
        var mostPlayed = trackStatistics.stream().max(Comparator.comparingInt(TrackStats::getTimesPlayed));
        stats.setMostPlayedTrack(mostPlayed.map(trackMapper::toMostPlayedTrack).orElse(null));

        var resp =  new PlaylistStatisticsResp();
        resp.setPlaylistStatistics(stats);
        return resp;
    }

    private PlaybackStats getPlaybackStatsFromSession() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());

                var statsOpt = playbackStatsRepository.findByOwner(sessionUser);

                if (statsOpt.isEmpty()) {
                    throw new NotFoundException("playback stats not found for user: " + sessionUser.getId());
                }

                return statsOpt.get();
            }
        }

        throw new UnauthorizedException("No authenticated user found in session");
    }
}
