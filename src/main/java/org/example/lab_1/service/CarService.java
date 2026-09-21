package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.example.lab_1.model.Car;
import org.example.lab_1.model.HumanBeing;
import org.example.lab_1.dto.CarRequest;
import org.example.lab_1.dto.CarResponse;
import org.example.lab_1.mapper.CarMapper;
import org.example.lab_1.repository.HumanRepository;
import org.example.lab_1.repository.CarRepository;

@ApplicationScoped
@Transactional
public class CarService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CarRepository carRepository;
    @Inject
    CarMapper mapper;

    public List<CarResponse> list() {
        List<CarResponse> result = new ArrayList<>();
        for (Car car : carRepository.findAll()) {
            result.add(mapper.toResponse(car));
        }
        return result;
    }

    public CarResponse get(long id) {
        return mapper.toResponse(carRepository.findById(id));
    }

    public CarResponse create(CarRequest input) {
        Car car = new Car();
        car.name = input.getName();
        car.cool = input.getCool();
        car.color = input.getColor();
        carRepository.save(car);
        return mapper.toResponse(car);
    }

    public CarResponse update(long id, CarRequest input) {
        Car car = carRepository.findById(id);
        car.name = input.getName();
        car.cool = input.getCool();
        car.color = input.getColor();
        return mapper.toResponse(car);
    }

    public void delete(long id, Long replacementId) {
        Car car = carRepository.findById(id);
        if (replacementId != null && replacementId == id) {
            throw new BadRequestException("Выберите другой автомобиль");
        }
        List<HumanBeing> humans = new ArrayList<>();
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.car != null && human.car.id == id) {
                humans.add(human);
            }
        }
        if (!humans.isEmpty() && replacementId == null) {
            throw new WebApplicationException("Автомобиль используется. Выберите замену.", 409);
        }
        Car replacement = replacementId == null ? null : carRepository.findById(replacementId);
        for (HumanBeing human : humans) {
            human.car = replacement;
        }
        carRepository.delete(car);
    }
}
