package org.example.popitkan5.games.exception;

public class MatchNotFoundException extends RuntimeException {
    
    public MatchNotFoundException(String message) {
        super(message);
    }
    
    public MatchNotFoundException(Long id) {
        super("Матч не найден с ID: " + id);
    }
}
