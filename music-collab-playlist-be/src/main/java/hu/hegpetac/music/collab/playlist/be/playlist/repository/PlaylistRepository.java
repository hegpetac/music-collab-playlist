package hu.hegpetac.music.collab.playlist.be.playlist.repository;

import hu.hegpetac.music.collab.playlist.be.playlist.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
