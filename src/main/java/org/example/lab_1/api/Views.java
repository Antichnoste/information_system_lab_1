package org.example.lab_1.api;
import org.example.lab_1.model.*;
import java.util.List;
public final class Views {
 private Views() {}
 public record CarView(Long id, long version, String name, boolean cool, String color) {}
 public record CoordinatesView(Long id, long version, String x, Float y) {}
 public record HumanView(Integer id, long version, String name, CoordinatesView coordinates,
  String creationDate, Boolean realHero, boolean hasToothpick, CarView car, Mood mood,
  String impactSpeed, String soundtrackName, String minutesOfWaiting, WeaponType weaponType) {}
 public record Page<T>(List<T> items, long total, int page, int size) {}
 public record Changed(int count, Integer id) {}
 public static CarView car(Car c) { return c == null ? null : new CarView(c.id,c.version,c.name,c.cool,c.color); }
 public static CoordinatesView coordinates(Coordinates c) { return new CoordinatesView(c.id,c.version,c.x.toString(),c.y); }
 public static HumanView human(HumanBeing h) {
  return new HumanView(h.id,h.version,h.name,coordinates(h.coordinates),h.creationDate.toString(),
   h.realHero,h.hasToothpick,car(h.car),h.mood,Long.toString(h.impactSpeed),h.soundtrackName,
   h.minutesOfWaiting == null ? null : h.minutesOfWaiting.toString(),h.weaponType);
 }
}
