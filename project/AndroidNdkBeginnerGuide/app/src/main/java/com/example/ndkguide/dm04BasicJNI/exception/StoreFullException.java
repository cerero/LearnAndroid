package com.example.ndkguide.dm04BasicJNI.exception;

public class StoreFullException extends RuntimeException {
    public StoreFullException(String pDetailMessage) {
        super(pDetailMessage);
    }
}
