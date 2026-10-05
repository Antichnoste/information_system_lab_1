package org.example.lab_1.service;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.lab_1.model.HumanBeing;
import org.example.lab_1.dto.HumanRequest;
import org.example.lab_1.dto.HumanResponse;
import org.example.lab_1.dto.HumanPageResponse;
import org.example.lab_1.mapper.HumanMapper;
import org.example.lab_1.repository.HumanRepository;
import org.example.lab_1.repository.CarRepository;
import org.example.lab_1.repository.CoordinatesRepository;

@ApplicationScoped
@Transactional
public class HumanService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CarRepository carRepository;
    @Inject
    CoordinatesRepository coordinatesRepository;
    @Inject
    HumanMapper mapper;

    public HumanPageResponse list(int page, int size, String sort, String direction, Map<String, String> filters) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Номер страницы должен быть от 0, размер — от 1 до 100, смещение — не больше 2147483647"));
        }
        if (sort == null || !List.of("id", "name", "soundtrackName", "carName", "carColor", "mood", "weaponType").contains(sort)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Недопустимое поле сортировки"));
        }

        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Направление сортировки: asc или desc"));
        }

        long total = humanRepository.count(filters);
        List<HumanResponse> result = new ArrayList<>();
        if ((long) page * size < total) {
            for (HumanBeing human : humanRepository.findPage(filters, sort, direction, page, size)) {
                result.add(mapper.toResponse(human));
            }
        }
        return new HumanPageResponse(result, total, page, size);
    }

    public HumanResponse get(int id) {
        return mapper.toResponse(humanRepository.findById(id));
    }

    public HumanResponse create(HumanRequest input) {
        HumanBeing human = new HumanBeing();
        fill(human, input);
        humanRepository.save(human);
        return mapper.toResponse(human);
    }

    public HumanResponse update(int id, HumanRequest input) {
        HumanBeing human = humanRepository.findById(id);
        if (input.getVersion() == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!input.getVersion().equals(human.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409,
                    "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        fill(human, input);
        humanRepository.flush();
        return mapper.toResponse(human);
    }

    public void delete(int id) {
        humanRepository.delete(humanRepository.findById(id));
    }

    private void fill(HumanBeing human, HumanRequest input) {
        human.name = input.getName();
        human.coordinates = coordinatesRepository.findById(input.getCoordinatesId());
        human.realHero = input.getRealHero();
        human.hasToothpick = input.getHasToothpick();
        human.car = input.getCarId() == null ? null : carRepository.findById(input.getCarId());
        human.mood = input.getMood();
        human.impactSpeed = input.getImpactSpeed();
        human.soundtrackName = input.getSoundtrackName();
        human.minutesOfWaiting = input.getMinutesOfWaiting();
        human.weaponType = input.getWeaponType();
    }
}
