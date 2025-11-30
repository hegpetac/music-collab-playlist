package hu.hegpetac.music.collab.playlist.be.authentication.filter;

import hu.hegpetac.music.collab.playlist.be.authentication.entity.OAuthProvider;
import hu.hegpetac.music.collab.playlist.be.authentication.entity.User;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RequireBothProvidersFilter extends OncePerRequestFilter  {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/oauth2/")
                || path.startsWith("/login")
                || path.startsWith("/logout")
                || path.startsWith("/connect")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!path.startsWith("/api/full/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = null;
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User user = (OAuth2User) auth.getPrincipal();
            Object e = user.getAttribute("email");
            if (e != null) email = e.toString();
        }

        if (email == null) {
            response.sendRedirect("/connect");
            return;
        }

        User localUser = userRepository.findByEmail(email).orElse(null);
        if (localUser == null || !(localUser.getEmail() != null && localUser.getSpotifyPrincipalId() != null)) {
            response.sendRedirect("/connect");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
