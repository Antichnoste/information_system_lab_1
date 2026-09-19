package org.example.lab_1.api;
import jakarta.validation.constraints.*;
import org.example.lab_1.model.*;
public final class Inputs {
 private Inputs() {}
 public static class Human {
  @NotNull @Size(min=1) public String name;
  @NotNull @Positive public Long coordinatesId;
  @NotNull public Boolean realHero;
  @NotNull public Boolean hasToothpick;
  @Positive public Long carId;
  public Mood mood;
  @NotNull public Long impactSpeed;
  @NotNull public String soundtrackName;
  public Long minutesOfWaiting;
  @NotNull public WeaponType weaponType;
  @PositiveOrZero public Long version;
 }
 public static class CarInput {
  public String name;
  @NotNull public Boolean cool;
  public String color;
  @PositiveOrZero public Long version;
 }
 public static class CoordinatesInput {
  @NotNull public Long x;
  @NotNull @DecimalMin(value="-629",inclusive=false) public Float y;
  @PositiveOrZero public Long version;
  @AssertTrue(message="y должно быть конечным числом")
  public boolean isFiniteY() { return y == null || Float.isFinite(y); }
 }
 public static class Login {
  @NotBlank public String username;
  @NotBlank public String password;
 }
}
