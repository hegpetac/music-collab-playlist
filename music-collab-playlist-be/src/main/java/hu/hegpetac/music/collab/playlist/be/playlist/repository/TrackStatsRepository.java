package hu.hegpetac.music.collab.playlist.be.playlist.repository;

import hu.hegpetac.music.collab.playlist.be.playlist.entity.TrackStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackStatsRepository extends JpaRepository<TrackStats, Long> {
}
