package ru.yandex.practicum.filmorate.exception;

public class DataRetrievalException extends RuntimeException {
    public DataRetrievalException(String message) {
        super(message);
    }

    public DataRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}