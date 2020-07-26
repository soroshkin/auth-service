package com.icl.auth.exception;

public class TokenBlockedException extends Exception{
    public TokenBlockedException() {
    }

    public TokenBlockedException(String message) {
        super(message);
    }

    public TokenBlockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenBlockedException(Throwable cause) {
        super(cause);
    }
}
