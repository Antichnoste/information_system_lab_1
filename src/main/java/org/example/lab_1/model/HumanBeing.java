package org.example.lab_1.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import org.hibernate.annotations.Check;
@Entity
@Table(name="human_being")
@Check(constraints = "id > 0 and name <> ''")
public class HumanBeing {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer id;

    @NotEmpty
    @Column(nullable=false)
    public String name;

    @NotNull
    @ManyToOne(optional=false)
    @JoinColumn(name="coordinates_id", nullable=false)
    public Coordinates coordinates;

    @NotNull
    @Column(name="creation_date", nullable=false)
    public Instant creationDate;

    @NotNull
    @Column(name="real_hero", nullable=false)
    public Boolean realHero;

    @Column(name="has_toothpick", nullable=false)
    public boolean hasToothpick;

    @ManyToOne
    @JoinColumn(name="car_id")
    public Car car;

    @Enumerated(EnumType.STRING)
    public Mood mood;

    @Column(name="impact_speed", nullable=false)
    public long impactSpeed;

    @NotNull
    @Column(name="soundtrack_name", nullable=false)
    public String soundtrackName;

    @Column(name="minutes_of_waiting")
    public Long minutesOfWaiting;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="weapon_type", nullable=false)
    public WeaponType weaponType;

    @PrePersist
    void created() {
        creationDate = Instant.now();
    }
}
