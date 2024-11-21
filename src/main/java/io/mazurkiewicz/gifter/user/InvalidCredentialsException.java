package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException
{
    public InvalidCredentialsException(String email) {
        super("Niepoprawne dane logowania dla %s".formatted(email), "GFTR_1001", new Object[] {email});
    }
}
