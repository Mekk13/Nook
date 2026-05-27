package com.Nook.backend.exception;
// Thrown when an action conflicts with the current state of the data.
// Examples:
//   - registering with an email that already exists
//   - trying to join a room you're already in
//   - trying to start a session when one is already running
// Maps to HTTP 409 Conflict in the GlobalExceptionHandler.

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
