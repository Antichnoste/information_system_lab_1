package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
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
        coordinates.x = input.getX();
        coordinates.y = input.getY();
        return mapper.toResponse(coordinates);
    }

    // При удалении используемых координат переносим ссылки на выбранную замену.
    public void delete(long id, Long replacementId) {
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (replacementId != null && replacementId == id) {
            throw new BadRequestException("Выберите другие координаты");
        }
        List<HumanBeing> humans = new ArrayList<>();
        for (HumanBeing human : humanRepository.findAll()) {
            if (human.coordinates != null && human.coordinates.id == id) {
                humans.add(human);
            }
        }
        if (!humans.isEmpty() && replacementId == null) {
            throw new WebApplicationException("Координаты используются. Выберите замену.", 409);
        }
        Coordinates replacement = replacementId == null ? null : coordinatesRepository.findById(replacementId);
        for (HumanBeing human : humans) {
            human.coordinates = replacement;
        }
        coordinatesRepository.flush();
        coordinatesRepository.delete(coordinates);
    }
}
