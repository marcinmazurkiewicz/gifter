package io.mazurkiewicz.gifter.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiError(HttpStatus status, String message, String errorCode) {

    ResponseEntity<Object> toResponse() {
        return new ResponseEntity<>(this, new HttpHeaders(), this.status());
    }
}
