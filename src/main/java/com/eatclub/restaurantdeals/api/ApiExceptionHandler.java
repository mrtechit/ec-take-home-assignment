package com.eatclub.restaurantdeals.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eatclub.restaurantdeals.service.InvalidTimeOfDayException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidTimeOfDayException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTimeOfDay(InvalidTimeOfDayException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid timeOfDay");
        return ResponseEntity.badRequest().body(problem);
    }
}
