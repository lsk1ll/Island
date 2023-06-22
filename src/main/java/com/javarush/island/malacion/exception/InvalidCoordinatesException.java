package com.javarush.island.malacion.exception;

public class InvalidCoordinatesException extends RuntimeException{
    public InvalidCoordinatesException() {
    }

    public InvalidCoordinatesException(String message) {
        super(message);
    }

    public InvalidCoordinatesException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCoordinatesException(Throwable cause) {
        super(cause);
    }
}
