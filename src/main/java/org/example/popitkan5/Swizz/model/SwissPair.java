package org.example.popitkan5.Swizz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwissPair {
    private User player1;
    private User player2;
    private boolean isBye;
    private int round;
}
