package hu.hegpetac.music.collab.playlist.be.playlist.repository;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.playlist.entity.PlaybackStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaybackStatsRepository extends JpaRepository<PlaybackStats, Long> {
    Optional<PlaybackStats> findByOwner(User user);
}
