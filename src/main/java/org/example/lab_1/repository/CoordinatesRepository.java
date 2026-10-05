package org.example.lab_1.repository;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.example.lab_1.model.Coordinates;

@ApplicationScoped
public class CoordinatesRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<Coordinates> findAll() {
        return em.createQuery("SELECT e FROM Coordinates e ORDER BY e.id", Coordinates.class)
                .getResultList();
    }

    public Coordinates findById(long id) {
        Coordinates coordinates = em.find(Coordinates.class, id);
        if (coordinates == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(404, "Координаты не найдены"));
        }
        return coordinates;
    }

    public void save(Coordinates coordinates) {
        em.persist(coordinates);
    }

    // Обновляем версию до формирования ответа клиенту.
    public void flush() {
        em.flush();
    }

    public void delete(Coordinates coordinates) {
        em.remove(coordinates);
    }
}
