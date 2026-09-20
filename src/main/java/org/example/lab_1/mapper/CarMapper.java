package org.example.lab_1.mapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab_1.dto.CarResponse;
import org.example.lab_1.model.Car;

@ApplicationScoped
public class CarMapper {
    public CarResponse toResponse(Car car) {
        if (car == null) {
            return null;
        }
        return new CarResponse(car.id, car.name, car.cool, car.color);
    }
}
