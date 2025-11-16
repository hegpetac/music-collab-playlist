package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthAccount;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.OAuthAccountRepository;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        User existingSessionUser = getAuthenticatedUserFromSession();

        User user;

        if (provider == OAuthProvider.GOOGLE) {
            System.out.println("Inside goole auth FLOW IN CUSTOM SERVICE");
            System.out.println("Google user: " + email);
            Optional<User> existingGoogleUser = userRepository.findByEmail(email);
            if (existingGoogleUser.isPresent()) {
                user = existingGoogleUser.get();
            } else {
                user = existingSessionUser != null ? existingSessionUser : new User();
                user.setEmail(email);
                user.setUsername(oauth2User.getAttribute("name"));
                user = userRepository.save(user);
            }
        } else {
            Optional<OAuthAccount> existingSpotifyAccount = accountRepository.findByProviderAndProviderUserId(provider, providerUserId);
            if (existingSpotifyAccount.isPresent()) {
                user = existingSpotifyAccount.get().getUser();
                OAuthAccount account = existingSpotifyAccount.get();
                updateSpotifyTokens(account, userRequest);
                accountRepository.save(account);
            } else {
                user = existingSessionUser != null ? existingSessionUser : new User();
                user.setDisplayName(oauth2User.getAttribute("display_name"));
                OAuthAccount account = new OAuthAccount();
                account.setProvider(provider);
                account.setProviderUserId(providerUserId);
                updateSpotifyTokens(account, userRequest);
                accountRepository.save(account);
                user.setSpotifyAccount(account);
                user = userRepository.save(user);
            }
        }

        System.out.println("BEFORE RETURNING CUSTOM USER: " + user);
        return new CustomOAuth2User(oauth2User, user);
    }

    private User getAuthenticatedUserFromSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                User sessionUser = customOAuth2User.getUser();
                System.out.println("Found authenticated user in session: " + sessionUser.getId());
                return sessionUser;
            }
        }

        System.out.println("No authenticated user found in session");
        return null;
    }

    private void updateSpotifyTokens(OAuthAccount account, OAuth2UserRequest userRequest) {
        account.setAccessToken(userRequest.getAccessToken().getTokenValue());
        account.setAccessTokenExpiresAt(userRequest.getAccessToken().getExpiresAt());

        try {
            String refreshTokenString = (String) userRequest.getAdditionalParameters().get("refresh_token");
            if (refreshTokenString != null) {
                OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(refreshTokenString, Instant.now());
                account.setRefreshToken(refreshToken.getTokenValue());
                System.out.println("Updated SPOTIFY refresh token");
            }
        } catch (Exception e) {
            System.err.println("Error updating SPOTIFY refresh token: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
