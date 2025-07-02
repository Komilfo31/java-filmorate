package ru.yandex.practicum.filmorate.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;


public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException ex) {
        return new ErrorResponse(
                "Validation error",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConditionsNotMetException(ConditionsNotMetException ex) {
        return new ErrorResponse(
                "Business logic error",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        return new ErrorResponse(
                "Not found",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtException(Exception ex, WebRequest request) {
        return new ErrorResponse(
                "Internal Server Error",
                "Unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleIllegalStateException(IllegalStateException ex) {
        return new ErrorResponse(
                "Internal Server Error",
                "System integrity violation: " + ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleNullPointerException(NullPointerException ex) {
        return new ErrorResponse(
                "Internal Server Error",
                "Null reference encountered: " + ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAlreadyFriendsException(AlreadyFriendsException ex) {
        return new ErrorResponse(
                "Friends error",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFriendsException(NotFriendsException ex) {
        return new ErrorResponse(
                "Friends error",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDataAccessException(DataAccessException ex) {
        return new ErrorResponse(
                "Database error",
                "Ошибка при работе с базой данных: " + ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataRetrievalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDataRetrievalException(DataRetrievalException ex) {
        return new ErrorResponse(
                "Data retrieval error",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistsException(AlreadyExistsException ex) {
        return new ErrorResponse(
                "Already exists",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
}