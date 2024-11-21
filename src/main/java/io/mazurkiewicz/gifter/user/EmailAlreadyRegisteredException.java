package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.NotValidException;

public class EmailAlreadyRegisteredException extends NotValidException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email jest już przypisany do istniejącego konta", "GFTR_1003", new Object[] {email});
    }
}
