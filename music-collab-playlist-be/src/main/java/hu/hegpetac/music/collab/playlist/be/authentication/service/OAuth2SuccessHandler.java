package hu.hegpetac.music.collab.playlist.be.authentication.service;

import hu.hegpetac.music.collab.playlist.be.authentication.principal.CustomUserPrincipal;
import hu.hegpetac.music.collab.playlist.be.authentication.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal.getUser(), List.of("authenticated"), 86400000L);
        String redirectUrl = "http://localhost:4200/playlist?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
