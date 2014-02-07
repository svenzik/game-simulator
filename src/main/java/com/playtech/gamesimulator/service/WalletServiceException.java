package com.playtech.gamesimulator.service;


public class WalletServiceException extends Exception {

    public WalletServiceException(String message) {
        super(message);
    }

    public WalletServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
