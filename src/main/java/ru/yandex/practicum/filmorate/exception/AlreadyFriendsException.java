package ru.yandex.practicum.filmorate.exception;

public class AlreadyFriendsException extends RuntimeException {
    public AlreadyFriendsException(String message) {
        super(message);
    }
}