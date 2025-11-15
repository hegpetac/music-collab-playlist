package hu.hegpetac.music.collab.playlist.be.authentication.repository;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthAccount;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    List<OAuthAccount> findByUser(User user);
}
