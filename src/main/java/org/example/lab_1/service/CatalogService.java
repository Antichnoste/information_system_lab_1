package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;
import org.example.lab_1.api.*;
import org.example.lab_1.model.*;
import org.example.lab_1.repository.CatalogRepository;
import static org.example.lab_1.api.Views.*;

@ApplicationScoped @Transactional
public class CatalogService {
 @Inject CatalogRepository repo;
 @Inject Event<DomainChange> changes;
 private void changed() { repo.flush(); changes.fire(new DomainChange()); }
 private void version(long actual,Long expected) {
  if(expected==null) throw new ApiException(400,"Укажите версию объекта","version");
  if(actual!=expected) throw new ApiException(409,"Объект уже изменён другим пользователем. Обновите данные.","version");
 }
 public Page<HumanView> humans(int page,int size,String sort,String direction,Map<String,String> filters) {
  if(page<0 || size<1 || size>100 || (long)page*size>Integer.MAX_VALUE)
   throw new ApiException(400,"Некорректная страница: page >= 0, size от 1 до 100");
  Map<String,String> columns=Map.of("id","h.id","name","h.name","soundtrackName","h.soundtrackName",
   "carName","c.name","carColor","c.color","mood","cast(h.mood as String)","weaponType","cast(h.weaponType as String)");
  if(!columns.containsKey(sort) || !(direction.equals("asc") || direction.equals("desc")))
   throw new ApiException(400,"Недопустимое поле или направление сортировки");
  StringBuilder where=new StringBuilder(" where 1=1");
  Map<String,String> params=new HashMap<>();
  filters.forEach((key,value)->{
   if(value!=null && !value.isEmpty()) {
    where.append(" and lower(").append(columns.get(key)).append(") like :").append(key).append(" escape '!'");
    params.put(key,CatalogRepository.containsPattern(value));
   }
  });
  String from=" from HumanBeing h left join h.car c";
  var count=repo.query("select count(h)"+from+where,Long.class);
  var query=repo.query("select h"+from+where+" order by "+columns.get(sort)+" "+direction+(sort.equals("id")?"":", h.id asc"),HumanBeing.class);
  params.forEach((k,v)->{ count.setParameter(k,v); query.setParameter(k,v); });
  var items=query.setFirstResult(page*size).setMaxResults(size).getResultList().stream().map(Views::human).toList();
  return new Page<>(items,count.getSingleResult(),page,size);
 }
 public HumanView humanById(int id) { return human(repo.get(HumanBeing.class,id)); }
 public List<CarView> cars() { return repo.all(Car.class).stream().map(Views::car).toList(); }
 public CarView carById(long id) { return car(repo.get(Car.class,id)); }
 public List<CoordinatesView> coordinates() { return repo.all(Coordinates.class).stream().map(Views::coordinates).toList(); }
 public CoordinatesView coordinatesById(long id) { return Views.coordinates(repo.get(Coordinates.class,id)); }
 private void fill(HumanBeing h,Inputs.Human in) {
  h.name=in.name; h.coordinates=repo.get(Coordinates.class,in.coordinatesId);
  h.realHero=in.realHero; h.hasToothpick=in.hasToothpick;
  h.car=in.carId==null?null:repo.get(Car.class,in.carId);
  h.mood=in.mood; h.impactSpeed=in.impactSpeed; h.soundtrackName=in.soundtrackName;
  h.minutesOfWaiting=in.minutesOfWaiting; h.weaponType=in.weaponType;
 }
 public HumanView createHuman(Inputs.Human in) {
  repo.lockWrites(); var h=new HumanBeing(); fill(h,in); repo.save(h); changed(); return human(h);
 }
 public HumanView updateHuman(int id,Inputs.Human in) {
  repo.lockWrites(); var h=repo.get(HumanBeing.class,id); version(h.version,in.version);
  fill(h,in); changed(); return human(h);
 }
 public void deleteHuman(int id,Long expected) {
  repo.lockWrites(); var h=repo.get(HumanBeing.class,id); version(h.version,expected); repo.remove(h); changed();
 }
 public CarView createCar(Inputs.CarInput in) {
  repo.lockWrites(); var c=new Car(); c.name=in.name; c.cool=in.cool; c.color=in.color;
  repo.save(c); changed(); return car(c);
 }
 public CarView updateCar(long id,Inputs.CarInput in) {
  repo.lockWrites(); var c=repo.get(Car.class,id); version(c.version,in.version);
  c.name=in.name; c.cool=in.cool; c.color=in.color; changed(); return car(c);
 }
 public CoordinatesView createCoordinates(Inputs.CoordinatesInput in) {
  repo.lockWrites(); var c=new Coordinates(); c.x=in.x; c.y=in.y; repo.save(c); changed(); return Views.coordinates(c);
 }
 public CoordinatesView updateCoordinates(long id,Inputs.CoordinatesInput in) {
  repo.lockWrites(); var c=repo.get(Coordinates.class,id); version(c.version,in.version);
  c.x=in.x; c.y=in.y; changed(); return Views.coordinates(c);
 }
 public void deleteCar(long id,Long replacementId,Long expected) {
  repo.lockWrites(); var c=repo.get(Car.class,id); version(c.version,expected);
  if(Objects.equals(replacementId,id)) throw new ApiException(400,"Выберите другой автомобиль","replacementId");
  var people=repo.query("from HumanBeing where car.id=:id",HumanBeing.class).setParameter("id",id).getResultList();
  if(!people.isEmpty() && replacementId==null) throw new ApiException(409,"Автомобиль используется. Выберите замену.","replacementId");
  Car replacement=replacementId==null?null:repo.get(Car.class,replacementId);
  people.forEach(h->h.car=replacement);
  repo.flush(); repo.remove(c); changed();
 }
 public void deleteCoordinates(long id,Long replacementId,Long expected) {
  repo.lockWrites(); var c=repo.get(Coordinates.class,id); version(c.version,expected);
  if(Objects.equals(replacementId,id)) throw new ApiException(400,"Выберите другие координаты","replacementId");
  var people=repo.query("from HumanBeing where coordinates.id=:id",HumanBeing.class).setParameter("id",id).getResultList();
  if(!people.isEmpty() && replacementId==null) throw new ApiException(409,"Координаты используются. Выберите замену.","replacementId");
  Coordinates replacement=replacementId==null?null:repo.get(Coordinates.class,replacementId);
  people.forEach(h->h.coordinates=replacement);
  repo.flush(); repo.remove(c); changed();
 }
 public Changed deleteByWeapon(WeaponType weapon) {
  if(weapon==null) throw new ApiException(400,"Укажите тип оружия","weaponType");
  repo.lockWrites(); var list=repo.query("from HumanBeing where weaponType=:weapon order by id",HumanBeing.class)
   .setParameter("weapon",weapon).setMaxResults(1).getResultList();
  if(list.isEmpty()) return new Changed(0,null);
  var h=list.get(0); repo.remove(h); changed(); return new Changed(1,h.id);
 }
 public HumanView minimumWaiting() {
  return repo.query("from HumanBeing where minutesOfWaiting is not null order by minutesOfWaiting, id",HumanBeing.class)
   .setMaxResults(1).getResultList().stream().findFirst().map(Views::human).orElse(null);
 }
 public List<HumanView> soundtrack(String substring) {
  if(substring==null) throw new ApiException(400,"Укажите подстроку","substring");
  return repo.query("from HumanBeing where lower(soundtrackName) like :pattern escape '!' order by id",HumanBeing.class)
   .setParameter("pattern",CatalogRepository.containsPattern(substring)).getResultList().stream().map(Views::human).toList();
 }
 public Changed sadden() {
  repo.lockWrites(); var list=repo.query("from HumanBeing where realHero=true and (mood is null or mood<>:mood)",HumanBeing.class)
   .setParameter("mood",Mood.SORROW).getResultList();
  list.forEach(h->h.mood=Mood.SORROW); if(!list.isEmpty()) changed(); return new Changed(list.size(),null);
 }
 public Changed giveCars() {
  repo.lockWrites(); var list=repo.query("from HumanBeing where realHero=true and car is null",HumanBeing.class).getResultList();
  if(list.isEmpty()) return new Changed(0,null);
  var cars=repo.query("from Car where name=:name and color=:color order by id",Car.class)
   .setParameter("name","Lada Kalina").setParameter("color","RED").setMaxResults(1).getResultList();
  Car c;
  if(cars.isEmpty()) { c=new Car(); c.name="Lada Kalina"; c.color="RED"; c.cool=false; repo.save(c); }
  else c=cars.get(0);
  list.forEach(h->h.car=c); changed(); return new Changed(list.size(),null);
 }
}
