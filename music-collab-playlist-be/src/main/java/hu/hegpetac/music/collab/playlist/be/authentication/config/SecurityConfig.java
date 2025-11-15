package hu.hegpetac.music.collab.playlist.be.authentication.config;

import hu.hegpetac.music.collab.playlist.be.authentication.filter.RequireBothProvidersFilter;
import hu.hegpetac.music.collab.playlist.be.authentication.repository.UserRepository;
import hu.hegpetac.music.collab.playlist.be.authentication.service.CustomOAuth2UserService;
import hu.hegpetac.music.collab.playlist.be.authentication.service.PostLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                                        "/error"
                                ).permitAll()
                                .requestMatchers("/api/full/**").authenticated()
                                .anyRequest().authenticated()
                        //TODO tovabbi vegpontok felvetele kesobbiekben
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
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
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
    public OAuth2AuthorizedClientService authorizedClientService(
            DataSource dataSource,
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        JdbcOperations jdbcOperations = new JdbcTemplate(dataSource);
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clients,
                                                                 OAuth2AuthorizedClientService clientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, clientService);
        return manager;
    }

    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager manager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth2.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder()
                .apply(oauth2.oauth2Configuration())
                .build();
    }

}
