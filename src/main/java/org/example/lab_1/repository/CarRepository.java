package org.example.lab_1.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.example.lab_1.model.Car;

@ApplicationScoped
public class CarRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<Car> findAll() {
        return em.createQuery("select e from Car e order by e.id", Car.class)
                .getResultList();
    }

    public Car findById(long id) {
        Car car = em.find(Car.class, id);
        if (car == null) throw new NotFoundException("Автомобиль не найден");
        return car;
    }

    public void save(Car car) {
        em.persist(car);
    }

    public void delete(Car car) {
        em.remove(car);
    }

    // Записать новые ссылки до удаления старого объекта, не завершая транзакцию.
    public void flush() {
        em.flush();
    }
}
