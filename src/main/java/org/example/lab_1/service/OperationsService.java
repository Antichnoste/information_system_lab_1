package org.example.lab_1.service;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.example.lab_1.model.Car;
import org.example.lab_1.model.HumanBeing;
import org.example.lab_1.model.Mood;
import org.example.lab_1.model.WeaponType;
import org.example.lab_1.dto.HumanResponse;
import org.example.lab_1.dto.OperationResponse;
import org.example.lab_1.mapper.HumanMapper;
import org.example.lab_1.repository.HumanRepository;
import org.example.lab_1.repository.CarRepository;

@ApplicationScoped
@Transactional
public class OperationsService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CarRepository carRepository;
    @Inject
    HumanMapper mapper;

    // Удалить первого найденного персонажа с заданным оружием.
    public OperationResponse deleteByWeapon(WeaponType weapon) {
        if (weapon == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите тип оружия"));
        }

        HumanBeing human = humanRepository.findFirstByWeapon(weapon);
        if (human == null) {
            return new OperationResponse(0, null);
        }
        humanRepository.delete(human);
        return new OperationResponse(1, human.id);
    }

    // Найти минимальное время ожидания, пропуская null.
    public HumanResponse minimumWaiting() {
        HumanBeing minimum = humanRepository.findMinimumWaiting();
        return minimum == null ? null : mapper.toResponse(minimum);
    }

    // Поиск обычной подстроки без учёта регистра.
    public List<HumanResponse> soundtrack(String substring) {
        if (substring == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите подстроку"));
        }

        List<HumanResponse> result = new ArrayList<>();
        for (HumanBeing human : humanRepository.findBySoundtrack(substring)) {
            result.add(mapper.toResponse(human));
        }
        return result;
    }

    // Максимально печальное настроение для realHero = true.
    public OperationResponse sadden() {
        List<HumanBeing> humans = humanRepository.findHeroesToSadden();
        for (HumanBeing human : humans) {
            human.mood = Mood.SORROW;
        }
        return new OperationResponse(humans.size(), null);
    }

    // Назначить одну общую красную Lada Kalina героям без машины.
    public OperationResponse giveCars() {
        List<HumanBeing> humansWithoutCar = humanRepository.findHeroesWithoutCar();
        if (humansWithoutCar.isEmpty()){
            return new OperationResponse(0, null);
        }

        Car kalina = carRepository.findFirstByNameAndColor("Lada Kalina", "RED");
        if (kalina == null) {
            kalina = new Car();
            kalina.name = "Lada Kalina";
            kalina.color = "RED";
            kalina.cool = false;
            carRepository.save(kalina);
        }
        for (HumanBeing human : humansWithoutCar) {
            human.car = kalina;
        }
        return new OperationResponse(humansWithoutCar.size(), null);
    }
}
