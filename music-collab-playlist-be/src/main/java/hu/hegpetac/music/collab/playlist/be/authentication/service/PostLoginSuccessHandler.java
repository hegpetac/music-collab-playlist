package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PostLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public PostLoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
             user = customOAuth2User.getUser();
        }

        if (user != null && user.getEmail() != null && user.getSpotifyAccount() != null) {
            response.sendRedirect("/playlist");
        } else {
            response.sendRedirect("/connect");
        }
    }
}
