package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import hu.hegpetac.music.collab.playlist.be.dashboard.repository.DashboardSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final DashboardSettingsRepository dashboardSettingsRepository;

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
            Optional<User> existingSpotifyUser = userRepository.findBySpotifyPrincipalId(providerUserId);
            if (existingSpotifyUser.isPresent()) {
                user = existingSpotifyUser.get();
            } else {
                user = existingSessionUser != null ? existingSessionUser : new User();
                user.setDisplayName(oauth2User.getAttribute("display_name"));
                user.setSpotifyPrincipalId(providerUserId);
                user = userRepository.save(user);
            }
        }

        System.out.println("BEFORE RETURNING CUSTOM USER: GOOGLE account " + user.getEmail() + " SPOTIFY account " + user.getDisplayName());
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
}
