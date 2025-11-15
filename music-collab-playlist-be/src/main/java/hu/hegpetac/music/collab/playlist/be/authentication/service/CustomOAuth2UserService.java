package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthAccount;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.OAuthAccountRepository;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final OAuthAccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.GOOGLE.name().equalsIgnoreCase(registrationId) ? OAuthProvider.GOOGLE : OAuthProvider.SPOTIFY;

        String providerUserId = oauth2User.getAttribute("sub") != null ? oauth2User.getAttribute("sub") : oauth2User.getAttribute("id");
        String email = oauth2User.getAttribute("email");

        User user;

        if(provider == OAuthProvider.GOOGLE) {
            user = userRepository.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setUsername(oauth2User.getAttribute("name"));
                return userRepository.save(u);
            });
        } else {
            Optional<OAuthAccount> existing = accountRepository.findByProviderAndProviderUserId(provider, providerUserId);
            if (existing.isPresent()) {
                user = existing.get().getUser();
            } else {
                user = new User();
                user.setDisplayName(oauth2User.getAttribute("display_name"));

                User finalUser = user;
                OAuthAccount account = accountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                        .orElseGet(() -> {
                            OAuthAccount a = new OAuthAccount();
                            a.setProvider(provider);
                            a.setProviderUserId(providerUserId);
                            a.setUser(finalUser);
                            return a;
                        });

                account.setAccessToken(userRequest.getAccessToken().getTokenValue());
                account.setAccessTokenExpiresAt(userRequest.getAccessToken().getExpiresAt());
                OAuth2RefreshToken newRefreshToken = null;

                try {
                    String refreshTokenString = (String) userRequest.getAdditionalParameters().get("refresh_token");
                    if (refreshTokenString != null) {
                        newRefreshToken = new OAuth2RefreshToken(refreshTokenString, Instant.now());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (newRefreshToken != null) {
                    account.setRefreshToken(newRefreshToken.getTokenValue());
                }

                accountRepository.save(account);
                user.setSpotifyAccount(account);
                user = userRepository.save(user);
            }
        }

        return new CustomOAuth2User(oauth2User, user);
    }
}
