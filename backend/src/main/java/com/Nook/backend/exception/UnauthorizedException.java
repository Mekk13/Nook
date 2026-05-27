package com.Nook.backend.exception;
// Thrown when a user tries to do something they don't have permission for.
// Example: a MEMBER trying to delete a room they don't own.
// Maps to HTTP 403 Forbidden in the GlobalExceptionHandler.

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
