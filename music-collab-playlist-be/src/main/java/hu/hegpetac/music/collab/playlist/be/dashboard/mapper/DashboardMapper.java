package hu.hegpetac.music.collab.playlist.be.dashboard.mapper;

import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.SuggestionPlaybackMode;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.YoutubePlaybackMode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    DashboardSettings mapDashboardSettings(org.openapitools.model.DashboardSettings dashboardSettings);
    @Mapping(target = "qrBaseUrl", ignore = true)
    @Mapping(target = "replayOffsetLimit", source = "replayTimeLimit")
    org.openapitools.model.DashboardSettings mapDashboardSettings(DashboardSettings dashboardSettings);

    YoutubePlaybackMode mapYoutubePlaybackMode(org.openapitools.model.YoutubePlaybackMode youtubePlaybackMode);
    org.openapitools.model.YoutubePlaybackMode mapYoutubePlaybackMode(YoutubePlaybackMode youtubePlaybackMode);

    SuggestionPlaybackMode mapSuggestionPlaybackMode(org.openapitools.model.SuggestionPlaybackMode suggestionPlaybackMode);
    org.openapitools.model.SuggestionPlaybackMode mapSuggestionPlaybackMode(SuggestionPlaybackMode suggestionPlaybackMode);
}
