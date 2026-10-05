package org.example.lab_1.service;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.example.lab_1.model.Coordinates;
import org.example.lab_1.model.HumanBeing;
import org.example.lab_1.dto.CoordinatesRequest;
import org.example.lab_1.dto.CoordinatesResponse;
import org.example.lab_1.mapper.CoordinatesMapper;
import org.example.lab_1.repository.HumanRepository;
import org.example.lab_1.repository.CoordinatesRepository;

@ApplicationScoped
@Transactional
public class CoordinatesService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CoordinatesRepository coordinatesRepository;
    @Inject
    CoordinatesMapper mapper;

    public List<CoordinatesResponse> list() {
        List<CoordinatesResponse> result = new ArrayList<>();
        for (Coordinates coordinates : coordinatesRepository.findAll()) {
            result.add(mapper.toResponse(coordinates));
        }
        return result;
    }

    public CoordinatesResponse get(long id) {
        return mapper.toResponse(coordinatesRepository.findById(id));
    }

    public CoordinatesResponse create(CoordinatesRequest input) {
        Coordinates coordinates = new Coordinates();
        coordinates.x = input.getX();
        coordinates.y = input.getY();
        coordinatesRepository.save(coordinates);
        return mapper.toResponse(coordinates);
    }

    public CoordinatesResponse update(long id, CoordinatesRequest input) {
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (input.getVersion() == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!input.getVersion().equals(coordinates.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        coordinates.x = input.getX();
        coordinates.y = input.getY();
        coordinatesRepository.flush();
        return mapper.toResponse(coordinates);
    }

    public void delete(long id, Long replacementId) {
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (replacementId != null && replacementId == id) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Выберите другие координаты"));
        }
        List<HumanBeing> humans = humanRepository.findByCoordinatesId(id);
        if (!humans.isEmpty() && replacementId == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Координаты используются. Выберите замену."));
        }
        Coordinates replacement = replacementId == null ? null : coordinatesRepository.findById(replacementId);
        for (HumanBeing human : humans) {
            human.coordinates = replacement;
        }
        coordinatesRepository.delete(coordinates);
    }
}
