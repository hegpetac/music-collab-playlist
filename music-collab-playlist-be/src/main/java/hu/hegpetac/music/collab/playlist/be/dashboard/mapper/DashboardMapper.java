package hu.hegpetac.music.collab.playlist.be.dashboard.mapper;

import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.SuggestionPlaybackMode;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.YoutubePlaybackMode;
import org.mapstruct.Mapper;

@Mapper
public interface DashboardMapper {
    public DashboardSettings mapDashboardSettings(org.openapitools.model.DashboardSettings dashboardSettings);
    public org.openapitools.model.DashboardSettings mapDashboardSettings(DashboardSettings dashboardSettings);

    public YoutubePlaybackMode mapYoutubePlaybackMode(org.openapitools.model.YoutubePlaybackMode youtubePlaybackMode);
    public org.openapitools.model.YoutubePlaybackMode mapYoutubePlaybackMode(YoutubePlaybackMode youtubePlaybackMode);

    public SuggestionPlaybackMode mapSuggestionPlaybackMode(org.openapitools.model.SuggestionPlaybackMode suggestionPlaybackMode);
    public org.openapitools.model.SuggestionPlaybackMode mapSuggestionPlaybackMode(SuggestionPlaybackMode suggestionPlaybackMode);
}
