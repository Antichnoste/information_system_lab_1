package org.example.lab_1.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CarRequest {
    private String name;
    @NotNull(message = "Укажите, крутой ли автомобиль")
    private Boolean cool;
    private String color;
}
