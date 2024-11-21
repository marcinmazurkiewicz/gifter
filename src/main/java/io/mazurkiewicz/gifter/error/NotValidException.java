package io.mazurkiewicz.gifter.error;

public non-sealed abstract class NotValidException extends ApiException {

    public NotValidException(String message, String errorCode, Object[] args) {
        super(message, errorCode, args);
    }

}
