package hu.hegpetac.music.collab.playlist.be.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class ConnectController {

    @Value("${FRONTEND_BASEURL}")
    private String frontendBaseUrl;

    @GetMapping("/connect")
    public void redirectToFrontendConnect(HttpServletResponse response) throws IOException {
        System.out.println("Redirecting to frontend connect: " + frontendBaseUrl);
        response.sendRedirect(frontendBaseUrl + "/connect-accounts");
    }

}
