package org.example.lab_1.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
@Entity @Table(name="human_being")
public class HumanBeing {
 @jakarta.validation.constraints.Positive @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Version public long version;
 @NotNull @Size(min=1) @Column(nullable=false, columnDefinition="text") public String name;
 @NotNull @ManyToOne(optional=false) @JoinColumn(name="coordinates_id",nullable=false) public Coordinates coordinates;
 @NotNull @Column(name="creation_date",nullable=false,updatable=false) public Instant creationDate;
 @NotNull @Column(name="real_hero",nullable=false) public Boolean realHero;
 @Column(name="has_toothpick",nullable=false) public boolean hasToothpick;
 @ManyToOne(optional=true) @JoinColumn(name="car_id",nullable=true) public Car car;
 @Enumerated(EnumType.STRING) @Column(length=32) public Mood mood;
 @Column(name="impact_speed",nullable=false) public long impactSpeed;
 @NotNull @Column(name="soundtrack_name",nullable=false,columnDefinition="text") public String soundtrackName;
 @Column(name="minutes_of_waiting") public Long minutesOfWaiting;
 @NotNull @Enumerated(EnumType.STRING) @Column(name="weapon_type",nullable=false,length=32) public WeaponType weaponType;
 @PrePersist void created() { creationDate = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS); }
}
