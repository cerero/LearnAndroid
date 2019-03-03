package com.example.ndkguide.dm04BasicJNI.exception;

public class NotExistingKeyException extends Exception {
    public NotExistingKeyException(String pDetailMessage) {
        super(pDetailMessage);
    }
}
