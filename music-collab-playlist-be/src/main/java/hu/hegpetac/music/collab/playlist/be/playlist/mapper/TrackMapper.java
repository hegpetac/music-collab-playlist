package hu.hegpetac.music.collab.playlist.be.playlist.mapper;

import hu.hegpetac.music.collab.playlist.be.playlist.entity.TrackStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.TrackSummary;

@Mapper(componentModel = "spring")
public interface TrackMapper {

    @Mapping(target = "timesPlayed", constant = "1")
    @Mapping(target = "lastPlayedAt", expression = "java(java.time.Instant.now())")
    TrackStats toNewTrackStats(TrackSummary trackSummary);
}
