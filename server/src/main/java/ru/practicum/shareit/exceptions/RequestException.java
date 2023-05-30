package ru.practicum.shareit.exceptions;

public class RequestException extends RuntimeException {
    public RequestException(String message) {
        super(message);
    }
}
