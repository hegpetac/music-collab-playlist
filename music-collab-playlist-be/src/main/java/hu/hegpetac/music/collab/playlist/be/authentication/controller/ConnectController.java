package hu.hegpetac.music.collab.playlist.be.authentication.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class ConnectController {

    @Value("${FRONTEND_BASEURL}")
    private String frontendBaseUrl;

    @GetMapping("/connect")
    public void redirectToFrontendConnect(
            @RequestParam(value = "missing", required = false) String missingProviders,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl = frontendBaseUrl + "/link-account";
        if (missingProviders != null && !missingProviders.isEmpty()) {
            redirectUrl += "?missing=" + missingProviders;
        }

        System.out.println("Redirecting to frontend connect: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }

}
