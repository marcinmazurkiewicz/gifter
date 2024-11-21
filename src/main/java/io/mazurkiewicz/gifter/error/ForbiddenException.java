package io.mazurkiewicz.gifter.error;

public non-sealed class ForbiddenException extends ApiException {

    public ForbiddenException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public ForbiddenException(String message, String errorCode, Object[] args) {
        super(message, errorCode, args);
    }
}