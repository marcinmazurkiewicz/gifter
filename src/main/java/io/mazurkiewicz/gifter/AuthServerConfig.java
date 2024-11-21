package io.mazurkiewicz.gifter;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class AuthServerConfig {

    private final String introspectionUri;
    private final String clientId;
    private final String clientSecret;
    private final String keycloakAddress;
    private final String realmName;
    private final List<String> allowedOrigins;

    public AuthServerConfig(
            @Value("${security.oauth2.resourceserver.opaque-token.introspection-uri}") String introspectionUri,
            @Value("${security.oauth2.resourceserver.keycloak-url}") String keycloakUrl,
            @Value("${security.oauth2.resourceserver.keycloak-realm}") String keycloakRealm,
            @Value("${security.oauth2.resourceserver.opaque-token.client-id}") String clientId,
            @Value("${security.oauth2.resourceserver.opaque-token.client-secret}") String clientSecret,
            @Value("${security.cors.allowed-origins}") List<String> allowedOrigins
    ) {
        this.introspectionUri = introspectionUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.keycloakAddress = keycloakUrl;
        this.realmName = keycloakRealm;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf -> csrf.ignoringRequestMatchers("/**")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(header -> header.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Origin", "*")))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.opaqueToken(token -> token.introspector(introspector())));

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*");
            }
        };
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("PUT", "DELETE", "GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers", "Access-Control-Allow-Headers", "Access-Control-Allow-Origin", "Authorization"));
        configuration.setExposedHeaders(List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public OpaqueTokenIntrospector introspector() {
        OpaqueTokenIntrospector introspector = new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);

        return token -> {
            log.info("### Start introspector");
            OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);
            log.info("### get principal: {}", principal);
            final List<GrantedAuthority> clientAuthorities = new ArrayList<>(principal.getAuthorities());
            log.info("### get authorities: {}", clientAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining("' ")));

            final Object realmAccess = principal.getAttribute("realm_access");
            log.info("### realmAccess: {}", realmAccess);
            if (realmAccess instanceof LinkedHashMap) {
                log.info("### realmAccess instanceof LinkedHashMap");
                final Object roles = ((ArrayList<?>) ((LinkedHashMap<?, ?>) realmAccess).get("roles"));
                log.info("### roles");

                if (roles != null) {
                    List<SimpleGrantedAuthority> userAuthorities = ((List<String>) roles).stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    clientAuthorities.addAll(userAuthorities);
                    log.info("### userAuthorities: {}", userAuthorities);
                }
            }

            log.info("### end introspector");
            return new OAuth2IntrospectionAuthenticatedPrincipal(
                    principal.getName(),
                    principal.getAttributes(),
                    Collections.unmodifiableList(clientAuthorities)
            );
        };
    }

    @Bean("resourceServerRequestMatcher")
    public RequestMatcher resources() {
        return new RequestHeaderRequestMatcher(HttpHeaders.AUTHORIZATION);
    }


    @Bean
    public RealmResource keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakAddress)
                .realm(realmName)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build()
                .realm(realmName);
    }
}
