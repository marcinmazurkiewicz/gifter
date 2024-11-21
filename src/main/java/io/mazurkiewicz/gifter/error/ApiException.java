package io.mazurkiewicz.gifter.error;

import lombok.Getter;

@Getter
public abstract sealed class ApiException extends RuntimeException
        permits NotFoundException, NotValidException, UnauthorizedException, ForbiddenException {

    private final String message;
    private final String errorCode;
    private final Object[] args;

    protected ApiException(String message, String errorCode, Object[] args) {
        this(message, errorCode, null, args);
    }

    protected ApiException(String message, String errorCode, Throwable throwable, Object[] args) {
        super(message, throwable);
        this.message = message;
        this.errorCode = errorCode;
        this.args = args;
    }
}
