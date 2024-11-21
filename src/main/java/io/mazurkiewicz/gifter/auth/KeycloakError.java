package io.mazurkiewicz.gifter.auth;

import lombok.Getter;

@Getter
public enum KeycloakError {
    EMAIL_ALREADY_USED("User exists with same email");

    private final String errorMessage;

    KeycloakError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isEqual(String keycloakError) {
        return errorMessage.equals(keycloakError);
    }
}
