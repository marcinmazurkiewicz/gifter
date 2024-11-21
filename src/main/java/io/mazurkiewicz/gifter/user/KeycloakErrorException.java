package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.NotValidException;

public class KeycloakErrorException extends NotValidException {
    public KeycloakErrorException(String message) {
        super(message, "GFTR_1004", new Object[]{});
    }
}
