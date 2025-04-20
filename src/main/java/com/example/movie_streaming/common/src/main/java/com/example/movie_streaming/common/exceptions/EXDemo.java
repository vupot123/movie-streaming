package com.example.movie_streaming.common.exceptions;

public class EXDemo {
    public static class DemoException extends RuntimeException {
        public DemoException(String message) {
            super(message);
        }
    }
}
