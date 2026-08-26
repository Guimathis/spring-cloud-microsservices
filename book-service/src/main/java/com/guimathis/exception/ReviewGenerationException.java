package com.guimathis.exception;

public class ReviewGenerationException extends RuntimeException {

    public ReviewGenerationException(String message) {
        super(message);
    }

    public ReviewGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
