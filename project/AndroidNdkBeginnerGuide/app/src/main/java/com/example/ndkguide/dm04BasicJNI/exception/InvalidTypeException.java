package com.example.ndkguide.dm04BasicJNI.exception;

public class InvalidTypeException extends Exception {
    public InvalidTypeException(String pDetailMessage) {
        super(pDetailMessage);
    }
}
