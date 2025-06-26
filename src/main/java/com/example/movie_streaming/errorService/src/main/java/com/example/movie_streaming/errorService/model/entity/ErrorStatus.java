package com.example.movie_streaming.errorService.model.entity;

public enum ErrorStatus {
    UNCHECKED(0), CHECKED(1);
    private final int value;
    ErrorStatus(int value) { this.value = value; }
    public int getValue() { return value; }
}