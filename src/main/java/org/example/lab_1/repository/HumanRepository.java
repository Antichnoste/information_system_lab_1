package org.example.lab_1.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.example.lab_1.model.HumanBeing;

@ApplicationScoped
public class HumanRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<HumanBeing> findAll() {
        return em.createQuery("select e from HumanBeing e order by e.id", HumanBeing.class)
                .getResultList();
    }

    public HumanBeing findById(int id) {
        HumanBeing human = em.find(HumanBeing.class, id);
        if (human == null) {
            throw new NotFoundException("Персонаж не найден");
        }
        return human;
    }

    public void save(HumanBeing human) {
        em.persist(human);
    }

    public void delete(HumanBeing human) {
        em.remove(human);
    }
}
