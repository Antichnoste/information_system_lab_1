package org.example.lab_1.repository;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.example.lab_1.model.Car;

@ApplicationScoped
public class CarRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<Car> findAll() {
        return em.createQuery("SELECT e FROM Car e ORDER BY e.id", Car.class)
                .getResultList();
    }

    public Car findById(long id) {
        Car car = em.find(Car.class, id);
        if (car == null) throw new WebApplicationException(GlobalExceptionHandler.response(404, "Автомобиль не найден"));
        return car;
    }

    public Car findFirstByNameAndColor(String name, String color) {
        return em.createQuery("SELECT c FROM Car c WHERE c.name = :name AND c.color = :color ORDER BY c.id", Car.class)
                .setParameter("name", name)
                .setParameter("color", color)
                .setMaxResults(1)
                .getResultList().stream().findFirst().orElse(null);
    }

    public void save(Car car) {
        em.persist(car);
    }

    public void flush() {
        em.flush();
    }

    public void delete(Car car) {
        em.remove(car);
    }
}
