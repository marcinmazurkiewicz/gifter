package io.mazurkiewicz.gifter.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mazurkiewicz.gifter.user.EmailAlreadyRegisteredException;
import io.mazurkiewicz.gifter.user.InvalidCredentialsException;
import io.mazurkiewicz.gifter.user.KeycloakErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.mazurkiewicz.gifter.auth.KeycloakError.EMAIL_ALREADY_USED;

@Slf4j
@Service
public class KeycloakAuthService implements AuthService {

    private static final String DEFAULT_USERNAME_PREFIX = "gftr_";
    private static final String DEFAULT_USER_ROLE = "ROLE_USER";
    private static final String CLIENT_ID_PARAM_NAME = "client_id";
    private static final String CLIENT_SECRET_PARAM_NAME = "client_secret";
    private static final String REFRESH_TOKEN_PARAM_NAME = "refresh_token";

    private final String clientId;
    private final String clientSecret;
    private final String keycloakUrl;
    private final String keycloakLogoutUrl;
    private final String realmName;
    private final RealmResource keycloak;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public KeycloakAuthService(RealmResource keycloak,
                               @Value("${security.oauth2.resourceserver.keycloak-url}") String keycloakUrl,
                               @Value("${security.oauth2.resourceserver.keycloak-logout-url}") String keycloakLogoutUrl,
                               @Value("${security.oauth2.resourceserver.keycloak-realm}") String keycloakRealm,
                               @Value("${security.oauth2.resourceserver.opaque-token.client-id}") String clientId,
                               @Value("${security.oauth2.resourceserver.opaque-token.client-secret}") String clientSecret,
                               ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.keycloakUrl = keycloakUrl;
        this.keycloakLogoutUrl = keycloakLogoutUrl;
        this.realmName = keycloakRealm;
        this.keycloak = keycloak;
        this.objectMapper = objectMapper;
        restTemplate = new RestTemplate();
    }

    @Override
    public Token loginUser(String email, String password) {
        UserRepresentation user = findUserByEmail(email);
        String username = user.getUsername();
        return getTokensForUser(email, username, password);
    }

    @Override
    public void logoutUser(String token) {
        HttpEntity<MultiValueMap<String, String>> request = prepareLogoutRequest(token);
        restTemplate.postForEntity(keycloakLogoutUrl, request, Object.class);
    }

    @Override
    public Token registerUser(String name, String lastname, String email, String password) {
        CredentialRepresentation credential = createCredential(password);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(email);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setUsername(generateUsername());
        userRepresentation.setFirstName(name);
        userRepresentation.setLastName(lastname);
        userRepresentation.setCredentials(List.of(credential));
        userRepresentation.setEnabled(true);

        String userPublicId = createUser(email, userRepresentation);

        addDefaultUserRole(userPublicId);
        return getTokensForUser(email, userRepresentation.getUsername(), password);
    }

    private String createUser(String email, UserRepresentation userRepresentation) {
        Response response = keycloak.users()
                .create(userRepresentation);

        return validateUserCreation(email, response);
    }

    private String validateUserCreation(String email, Response response) {
        if (HttpStatus.CREATED.value() != response.getStatus()) {
            String keycloakError = readErrorMessage(response);
            if (EMAIL_ALREADY_USED.isEqual(keycloakError)) {
                throw new EmailAlreadyRegisteredException(email);
            } else {
                throw new KeycloakErrorException(keycloakError);
            }
        }
        return CreatedResponseUtil.getCreatedId(response);
    }

    private void addDefaultUserRole(String userPublicId) {
        UsersResource usersResource = keycloak.users();
        UserResource createdUserResource = usersResource.get(userPublicId);
        createdUserResource.roles()
                .realmLevel()
                .add(List.of(findDefaultUserRole()));
    }

    private static String generateUsername() {
        return DEFAULT_USERNAME_PREFIX + RandomStringUtils.randomAlphabetic(10);
    }

    private static CredentialRepresentation createCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setCreatedDate(ZonedDateTime.now().toEpochSecond());
        return credential;
    }

    private UserRepresentation findUserByEmail(String email) {
        List<UserRepresentation> users = keycloak.users()
                .searchByEmail(email, true);

        if (users.size() != 1) {
            throw new InvalidCredentialsException(email);
        }

        return users.getFirst();
    }

    private Token getTokensForUser(String email, String username, String password) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realmName)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build()) {

            AccessTokenResponse accessToken = keycloak.tokenManager()
                    .getAccessToken();

            String token = accessToken.getToken();
            String refreshToken = accessToken.getRefreshToken();

            return new Token(token, refreshToken);
        } catch (NotAuthorizedException ex) {
            throw new InvalidCredentialsException(email);
        }
    }

    private String readErrorMessage(Response response) {
        try {
            return objectMapper.readValue((InputStream) response.getEntity(), Map.class)
                    .getOrDefault("errorMessage", "").toString();
        } catch (IOException e) {
            log.error("Error during read keycloak response: {} ", e.getMessage());
            return "";
        }
    }

    private RoleRepresentation findDefaultUserRole() {
        return keycloak.roles()
                .get(KeycloakAuthService.DEFAULT_USER_ROLE)
                .toRepresentation();
    }


    private HttpEntity<MultiValueMap<String, String>> prepareLogoutRequest(String token) {
        MultiValueMap<String, String> requestParams = prepareLogoutParams(token);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(requestParams, headers);
    }

    private MultiValueMap<String, String> prepareLogoutParams(String token) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add(CLIENT_ID_PARAM_NAME, clientId);
        requestParams.add(CLIENT_SECRET_PARAM_NAME, clientSecret);
        requestParams.add(REFRESH_TOKEN_PARAM_NAME, token);
        return requestParams;
    }

}
