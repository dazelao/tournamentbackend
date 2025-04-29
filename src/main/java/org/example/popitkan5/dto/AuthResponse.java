package org.example.popitkan5.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.model.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {

    private Long id;
    
    private String token;
    
    private String username;

    private Role role;
}
