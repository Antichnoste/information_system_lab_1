package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    // 1. Удалить первого найденного персонажа с заданным оружием.
    public OperationResponse deleteByWeapon(WeaponType weapon) {
        if (weapon == null) {
            throw new BadRequestException("Укажите тип оружия");
        }

        for (HumanBeing human : humanRepository.findAll()) {
            if (human.weaponType == weapon) {
                humanRepository.delete(human);
                return new OperationResponse(1, human.id);
            }
        }
        return new OperationResponse(0, null);
    }

    // 2. Найти минимальное время ожидания, пропуская null.
    public HumanResponse minimumWaiting() {
        HumanBeing minimum = null;
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.minutesOfWaiting == null) {
                continue;
            }

            if (minimum == null || human.minutesOfWaiting < minimum.minutesOfWaiting) {
                minimum = human;
            }
        }
        return minimum == null ? null : mapper.toResponse(minimum);
    }

    // 3. Поиск обычной подстроки без SQL-шаблонов и без учёта регистра.
    public List<HumanResponse> soundtrack(String substring) {
        if (substring == null) {
            throw new BadRequestException("Укажите подстроку");
        }

        String search = substring.toLowerCase(Locale.ROOT);
        List<HumanResponse> result = new ArrayList<>();
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.soundtrackName.toLowerCase(Locale.ROOT).contains(search)) {
                result.add(mapper.toResponse(human));
            }
        }
        return result;
    }

    // 4. Максимально печальное настроение только для realHero = true.
    public OperationResponse sadden() {
        int count = 0;
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.realHero && human.mood != Mood.SORROW) {
                human.mood = Mood.SORROW;
                count++;
            }
        }
        return new OperationResponse(count, null);
    }

    // 5. Назначить одну общую красную Lada Kalina героям без машины.
    public OperationResponse giveCars() {
        List<HumanBeing> humansWithoutCar = new ArrayList<>();
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.realHero && human.car == null) {
                humansWithoutCar.add(human);
            }
        }
        if (humansWithoutCar.isEmpty()){
            return new OperationResponse(0, null);
        }

        Car kalina = null;
        for (Car car : carRepository.findAll()) {
            if ("Lada Kalina".equals(car.name) && "RED".equals(car.color)) {
                kalina = car;
                break;
            }
        }
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
