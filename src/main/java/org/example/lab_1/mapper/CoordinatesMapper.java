package org.example.lab_1.mapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab_1.dto.CoordinatesResponse;
import org.example.lab_1.model.Coordinates;

@ApplicationScoped
public class CoordinatesMapper {
    public CoordinatesResponse toResponse(Coordinates coordinates) {
        // long передаём строкой, чтобы браузер не округлял большие целые числа.
        return new CoordinatesResponse(coordinates.id, coordinates.x.toString(), coordinates.y);
    }
}
