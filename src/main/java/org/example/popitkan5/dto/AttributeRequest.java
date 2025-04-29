package org.example.popitkan5.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeRequest {
    
    @NotBlank(message = "Ключ атрибута не может быть пустым")
    private String key;
    
    private String value;
}
