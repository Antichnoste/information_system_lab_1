package org.example.lab_1.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.example.lab_1.model.Coordinates;

@ApplicationScoped
public class CoordinatesRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<Coordinates> findAll() {
        return em.createQuery("select e from Coordinates e order by e.id", Coordinates.class)
                .getResultList();
    }

    public Coordinates findById(long id) {
        Coordinates coordinates = em.find(Coordinates.class, id);
        if (coordinates == null) {
            throw new NotFoundException("Координаты не найдены");
        }
        return coordinates;
    }

    public void save(Coordinates coordinates) {
        em.persist(coordinates);
    }

    public void delete(Coordinates coordinates) {
        em.remove(coordinates);
    }
}
