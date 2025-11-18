package hu.hegpetac.music.collab.playlist.be.dashboard.repository;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.dashboard.entity.DashboardSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardSettingsRepository extends JpaRepository<DashboardSettings, Long> {
    Optional<DashboardSettings> findByUser(User user);
    Optional<DashboardSettings> findByName(String name);

    Optional<DashboardSettings> findByDeviceCode(Integer deviceCode);
}
