package org.example.lab_1.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CoordinatesRequest {
    @NotNull(message = "Укажите координату x")
    private Long x;
    @NotNull(message = "Укажите координату y")
    @DecimalMin(value = "-629", inclusive = false, message = "Координата y должна быть больше -629")
    private Float y;
}
