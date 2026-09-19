package org.example.lab_1.dto;
import org.example.lab_1.model.Mood;
import org.example.lab_1.model.WeaponType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HumanResponse {
    private Integer id;
    private String name;
    private CoordinatesResponse coordinates;
    private String creationDate;
    private Boolean realHero;
    private boolean hasToothpick;
    private CarResponse car;
    private Mood mood;
    private String impactSpeed;
    private String soundtrackName;
    private String minutesOfWaiting;
    private WeaponType weaponType;
}
