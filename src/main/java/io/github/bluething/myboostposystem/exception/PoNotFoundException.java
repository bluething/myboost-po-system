package io.github.bluething.myboostposystem.exception;

public class PoNotFoundException extends RuntimeException {
    public PoNotFoundException(String message) {
        super(message);
    }

    public PoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
