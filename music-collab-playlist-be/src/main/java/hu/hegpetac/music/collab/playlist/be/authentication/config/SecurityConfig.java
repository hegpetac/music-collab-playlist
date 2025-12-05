package hu.hegpetac.music.collab.playlist.be.authentication.config;

import hu.hegpetac.music.collab.playlist.be.authentication.filter.RequireBothProvidersFilter;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import hu.hegpetac.music.collab.playlist.be.authentication.service.CustomOAuth2UserService;
import hu.hegpetac.music.collab.playlist.be.authentication.service.PostLoginSuccessHandler;
import hu.hegpetac.music.collab.playlist.be.authentication.service.SpotifyAuthorizedClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${FRONTEND_BASEURL}")
    private String frontendBaseUrl;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final PostLoginSuccessHandler postLoginSuccessHandler;
    private final UserRepository userRepository;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          PostLoginSuccessHandler postLoginSuccessHandler,
                          UserRepository userRepository) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.postLoginSuccessHandler = postLoginSuccessHandler;
        this.userRepository = userRepository;
    }

    @Bean
    public RequireBothProvidersFilter requireBothProvidersFilter() {
        return new RequireBothProvidersFilter(userRepository);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/",
                                        "/css/**",
                                        "/js/**",
                                        "/oauth2/**",
                                        "/connect/**",
                                        "/login/**",
                                        "/login/oauth2/**",
                                        "/error",
                                        "/join-playlist",
                                        "/suggest-track",
                                        "/search",
                                        "/ws/**"
                                ).permitAll()
                                .requestMatchers("/api/full/**").authenticated()
                                .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .successHandler(postLoginSuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .addFilterAfter(requireBothProvidersFilter(), OAuth2LoginAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendBaseUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Cache-Control", "Content-Type",
                "X-Requested-With", "Accept", "X-CSRF-TOKEN"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(SpotifyAuthorizedClientService service) {
        return service;
    }
}
