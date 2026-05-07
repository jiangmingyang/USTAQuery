package com.usta.query.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String uaid) {
        super("Player not found with UAID: " + uaid);
    }
}
