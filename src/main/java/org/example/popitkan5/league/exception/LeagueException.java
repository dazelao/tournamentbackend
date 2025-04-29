package org.example.popitkan5.league.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LeagueException extends RuntimeException {
    public LeagueException(String message) {
        super(message);
    }
}
