package com.bookingSystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> userAlreadyExistExceptionHandler(UserAlreadyExistsException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.IM_USED, exception.getMessage());
        problemDetail.setTitle("User already exists");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.IM_USED);
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<ProblemDetail> userDoesNotExistExceptionHandler(UserDoesNotExistException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("User doesn't exist");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceDoesNotExistException.class)
    public ResponseEntity<ProblemDetail> resourceDoesNotExistExceptionHandler(ResourceDoesNotExistException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Resource doesn't exist");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReservationDoesNotExistException.class)
    public ResponseEntity<ProblemDetail> reservationDoesNotExistExceptionHandler(ReservationDoesNotExistException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Reservation doesn't exist");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReservationAlreadyCancelledException.class)
    public ResponseEntity<ProblemDetail> reservationAlreadyCancelledExceptionHandler(ReservationAlreadyCancelledException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Reservation already cancelled");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(ReservationAlreadyConfirmedException.class)
    public ResponseEntity<ProblemDetail> reservationAlreadyConfirmedExceptionHandler(ReservationAlreadyConfirmedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Reservation already confirmed");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ProblemDetail> authenticationCredentialsNotFoundExceptionHandler(AuthenticationCredentialsNotFoundException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Authentication Failure");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid JSON request");
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timeStamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(UserIdConflictException.class)
    public ResponseEntity<ProblemDetail> userIdConflictExceptionHandler(UserIdConflictException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("User id conflict");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> authorizationDeniedExceptionHandler(AuthorizationDeniedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Authorization Failure");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> accessDeniedExceptionHandler(AccessDeniedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Access Denied!");
        problemDetail.setProperty("timeStamp", Instant.now());
        problemDetail.setDetail("Only ADMIN are allowed to access");
        return new ResponseEntity<>(problemDetail, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<ProblemDetail> invalidPriceExceptionHandler(InvalidPriceException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Invalid Price");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ProblemDetail> invalidStatusExceptionHandler(InvalidStatusException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Invalid Status");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ProblemDetail> invalidDateExceptionHandler(InvalidDateException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Invalid Date");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> illegalArgumentExceptionHandler(IllegalArgumentException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
        problemDetail.setTitle("Illegal Arguments");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> runtimeExceptionHandler(RuntimeException exception) {

        log.error("Unexpected runtime exception", exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );

        problemDetail.setTitle("Something went wrong");
        problemDetail.setProperty("timeStamp", Instant.now());

        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> globalExceptionHandler(Exception exception){
        log.error("Unexpected exception", exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
                );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timeStamp", Instant.now());
        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
