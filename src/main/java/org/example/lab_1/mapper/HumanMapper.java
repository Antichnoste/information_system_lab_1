package org.example.lab_1.mapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.lab_1.dto.HumanResponse;
import org.example.lab_1.model.HumanBeing;

@ApplicationScoped
public class HumanMapper {
    @Inject
    CarMapper carMapper;
    @Inject
    CoordinatesMapper coordinatesMapper;

    public HumanResponse toResponse(HumanBeing human) {
        // long передаём строкой, чтобы браузер не округлял большие целые числа.
        return new HumanResponse(
                human.id, human.name, coordinatesMapper.toResponse(human.coordinates),
                human.creationDate.toString(), human.realHero, human.hasToothpick,
                carMapper.toResponse(human.car), human.mood,
                Long.toString(human.impactSpeed), human.soundtrackName,
                human.minutesOfWaiting == null ? null : human.minutesOfWaiting.toString(),
                human.weaponType);
    }
}
