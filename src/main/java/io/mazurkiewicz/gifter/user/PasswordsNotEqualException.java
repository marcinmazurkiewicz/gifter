package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.NotValidException;

public class PasswordsNotEqualException extends NotValidException {
    public PasswordsNotEqualException() {
        super("Hasła nie są takie same", "GFTR_1002", new Object[]{});
    }
}
