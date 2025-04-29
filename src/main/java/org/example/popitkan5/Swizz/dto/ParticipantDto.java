package org.example.popitkan5.Swizz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.popitkan5.Swizz.model.SwissRegistration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private Long id;
    private Long userId;
    private String userName;
    private boolean active;
    
    /**
     * Создает DTO из сущности
     * @param registration сущность регистрации
     * @return DTO
     */
    public static ParticipantDto fromEntity(SwissRegistration registration) {
        return ParticipantDto.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .userName(registration.getUser().getUsername())
                .active(registration.isActive())
                .build();
    }
}