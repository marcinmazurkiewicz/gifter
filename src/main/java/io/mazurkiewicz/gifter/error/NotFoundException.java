package io.mazurkiewicz.gifter.error;

public non-sealed abstract class NotFoundException extends ApiException {

    public NotFoundException(String message, String errorCode, Object[] args) {
        super(message, errorCode, args);
    }
}
