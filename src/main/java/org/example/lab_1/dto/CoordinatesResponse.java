package org.example.lab_1.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesResponse {
    private Long id;
    private String x;
    private Float y;
}
