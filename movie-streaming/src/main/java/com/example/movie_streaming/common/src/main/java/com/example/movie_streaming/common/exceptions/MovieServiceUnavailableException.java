package com.example.movie_streaming.common.exceptions;

public class MovieServiceUnavailableException extends RuntimeException {
    public MovieServiceUnavailableException(String message) {
        super(message);
    }
}

