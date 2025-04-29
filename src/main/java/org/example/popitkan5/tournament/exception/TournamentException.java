package org.example.popitkan5.tournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TournamentException extends ResponseStatusException {

    public TournamentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public TournamentException(HttpStatus status, String message) {
        super(status, message);
    }
}
