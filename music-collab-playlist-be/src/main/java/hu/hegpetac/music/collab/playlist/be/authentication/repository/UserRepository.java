package hu.hegpetac.music.collab.playlist.be.authentication.repository;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
