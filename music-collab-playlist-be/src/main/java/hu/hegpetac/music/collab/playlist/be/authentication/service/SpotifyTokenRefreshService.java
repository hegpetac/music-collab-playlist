package hu.hegpetac.music.collab.playlist.be.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpotifyTokenRefreshService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final WebClient webClient = WebClient.create();

    public OAuth2AuthorizedClient refreshIfNeeded(String principal) {

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("spotify", principal);

        if (client == null || client.getRefreshToken() == null) {
            System.out.println("No client found");
            return null;
        }

        Instant expiresAt = client.getAccessToken().getExpiresAt();
        if (expiresAt != null && expiresAt.isAfter(Instant.now().plusSeconds(30))) {
            return client;
        }

        String refreshToken = client.getRefreshToken().getTokenValue();
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("spotify");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        SpotifyTokenResponse resp = webClient.post()
                .uri(registration.getProviderDetails().getTokenUri())
                .headers(h -> h.setBasicAuth(
                        registration.getClientId(),
                        registration.getClientSecret()
                ))
                .bodyValue(form)
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();

        Instant newIssuedAt = Instant.now();
        Instant newExpiresAt = newIssuedAt.plusSeconds(resp.expires_in());

        OAuth2AuthorizedClient updated = new OAuth2AuthorizedClient(
                registration,
                principal,
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        resp.access_token(),
                        newIssuedAt,
                        newExpiresAt,
                        resp.scope() != null
                                ? Set.of(resp.scope().split(" "))
                                : client.getAccessToken().getScopes()
                ),
                client.getRefreshToken()
        );
        System.out.println("New expires at: " + newExpiresAt);

        authorizedClientService.saveAuthorizedClient(updated, null);

        return updated;
    }

    record SpotifyTokenResponse(
            String access_token,
            int expires_in,
            String token_type,
            String scope
    ) {}
}
