package io.mazurkiewicz.gifter.error;

public non-sealed abstract class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message, String errorCode, Object[] args) {
        super(message, errorCode, args);
    }
}
