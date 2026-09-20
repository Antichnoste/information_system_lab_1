package org.example.lab_1.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.lab_1.model.AppUser;

@ApplicationScoped
public class AppUserRepository {
    @PersistenceContext(unitName = "humanity")
    private EntityManager em;

    public AppUser findByUsername(String username) {
        try {
            return em.createQuery(
                    "select u from AppUser u where u.username = :username",
                    AppUser.class
            )
            .setParameter("username", username)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public long count() {
        return em.createQuery("select count(u) from AppUser u", Long.class)
                .getSingleResult();
    }

    public void save(AppUser user) {
        em.persist(user);
    }
}
