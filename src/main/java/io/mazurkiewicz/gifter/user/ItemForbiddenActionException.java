package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.ForbiddenException;

public class ItemForbiddenActionException extends ForbiddenException {
    public ItemForbiddenActionException() {
        super("Nie masz uprawnień do usunięcia prezentu", "GDTR_1007");
    }
}
