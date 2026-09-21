package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Номер страницы должен быть от 0, размер — от 1 до 100");
        }
        if (sort == null || !List.of("id", "name", "soundtrackName", "carName", "carColor", "mood", "weaponType").contains(sort)) {
            throw new BadRequestException("Недопустимое поле сортировки");
        }

        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            throw new BadRequestException("Направление сортировки: asc или desc");
        }

        List<HumanBeing> humans = new ArrayList<>();
        for (HumanBeing human : humanRepository.findAll()) {
            if (matches(human, filters)) {
                humans.add(human);
            }
        }

        humans.sort((first, second) -> compareHumans(first, second, sort, direction));

        int from = (int) Math.min((long) page * size, humans.size());
        int to = Math.min(from + size, humans.size());
        
        List<HumanResponse> result = new ArrayList<>();
        for (int i = from; i < to; i++) {
            result.add(mapper.toResponse(humans.get(i)));
        }
        return new HumanPageResponse(result, humans.size(), page, size);
    }

    private int compareHumans(HumanBeing first, HumanBeing second, String sort, String direction) {
        int result;
        if ("id".equals(sort)) {
            result = Integer.compare(first.id, second.id);
        } else {
            result = text(first, sort).compareToIgnoreCase(text(second, sort));
        }

        if ("desc".equals(direction)) {
            result = -result;
        }

        if (result == 0) {
            return Integer.compare(first.id, second.id);
        }
        return result;
    }

    private boolean matches(HumanBeing human, Map<String, String> filters) {
        for (var filter : filters.entrySet()) {
            String value = filter.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            String actual = text(human, filter.getKey()).toLowerCase(Locale.ROOT);
            if (!actual.contains(value.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private String text(HumanBeing human, String column) {
        String value = switch (column) {
            case "name" -> human.name;
            case "soundtrackName" -> human.soundtrackName;
            case "carName" -> human.car == null ? null : human.car.name;
            case "carColor" -> human.car == null ? null : human.car.color;
            case "mood" -> human.mood == null ? null : human.mood.name();
            case "weaponType" -> human.weaponType.name();
            default -> throw new BadRequestException("Недопустимое строковое поле");
        };
        return value == null ? "" : value;
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
        fill(human, input);
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
