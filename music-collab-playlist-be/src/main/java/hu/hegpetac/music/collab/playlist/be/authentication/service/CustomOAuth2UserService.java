package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.AppUser;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.principal.CustomUserPrincipal;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String username;
        String email;

        if (provider == OAuthProvider.GOOGLE) {
            email = (String) attributes.get("email");
            username = (String) attributes.get("name");
        } else if (provider == OAuthProvider.SPOTIFY) {
            email =  (String) attributes.get("email");
            username = (String) attributes.get("display_name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setDisplayName(username);
                    newUser.setProvider(provider);
                    return userRepository.save(newUser);
                });

        user.setUsername(username);
        userRepository.save(user);

        return new CustomUserPrincipal(user, oAuth2User.getAttributes());
    }

}
