package com.example.movie_streaming.demoService.exception;
import com.example.movie_streaming.common.exceptions.EXDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.example.movie_streaming.common.exceptions.EXDemo.DemoException.class)
    public ResponseEntity<String> handleUserNotFound(EXDemo.DemoException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}