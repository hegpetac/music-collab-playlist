package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.SpotifyTokenEntity;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.SpotifyTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpotifyAuthorizedClientService implements OAuth2AuthorizedClientService {

    private final SpotifyTokenRepository repo;
    private final ClientRegistrationRepository registrations;

    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId, String principalName) {
        SpotifyTokenEntity entity = repo.findByUserId(principalName);
        if (entity == null) {
            return null;
        }

        ClientRegistration registration = registrations.findByRegistrationId(registrationId);
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                entity.accessToken(),
                entity.issuedAt(),
                entity.expiresAt(),
                Set.of(entity.scopes().split(" "))
        );
        OAuth2RefreshToken refreshToken = entity.refreshToken() == null ? null :
                new OAuth2RefreshToken(entity.refreshToken(), null);

        return (T) new OAuth2AuthorizedClient(
                registration,
                principalName,
                token,
                refreshToken
        );
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient client, Authentication principal) {

        SpotifyTokenEntity entity = new SpotifyTokenEntity(
                client.getPrincipalName(),
                client.getAccessToken().getTokenValue(),
                client.getRefreshToken() != null
                        ? client.getRefreshToken().getTokenValue()
                        : null,
                client.getAccessToken().getExpiresAt(),
                client.getAccessToken().getIssuedAt(),
                String.join(" ", client.getAccessToken().getScopes())
        );

        repo.save(entity);
    }

    @Override
    public void removeAuthorizedClient(String registrationId, String principalName) {
        repo.delete(principalName);
    }
}
