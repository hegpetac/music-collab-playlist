package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.model.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PostLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
             user = customOAuth2User.getUser();
        }

        if (user != null) {
            boolean hasGoogleLinked = user.getEmail() != null;
            boolean hasSpotifyLinked = user.getSpotifyPrincipalId() != null;

            if (hasGoogleLinked && hasSpotifyLinked) {
                response.sendRedirect("/dashboard");
            } else {
                List<String> missingProviders = new ArrayList<>();
                if (!hasGoogleLinked) missingProviders.add("google");
                if (!hasSpotifyLinked) missingProviders.add("spotify");
                response.sendRedirect("/connect?missing=" + String.join(",", missingProviders));
            }
        }
    }
}
