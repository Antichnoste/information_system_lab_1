package org.example.lab_1.repository;
import org.example.lab_1.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.example.lab_1.model.HumanBeing;
import org.example.lab_1.model.Mood;
import org.example.lab_1.model.WeaponType;

@ApplicationScoped
public class HumanRepository {
    private static final String SELECT = "SELECT h FROM HumanBeing h LEFT JOIN FETCH h.car c JOIN FETCH h.coordinates";

    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<HumanBeing> findPage(Map<String, String> filters, String sort, String direction, int page, int size) {
        Map<String, String> parameters = new HashMap<>();

        String where = filterConditions(filters, parameters);
        String column = "id".equals(sort) ? "h.id" : textColumn(sort);
        String order = "desc".equals(direction) ? "DESC" : "ASC";

        TypedQuery<HumanBeing> query = em.createQuery(SELECT + where + " ORDER BY " + column + " " + order + ", h.id ASC", HumanBeing.class);
        parameters.forEach((name, value) -> query.setParameter(name, value));

        return query.setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long count(Map<String, String> filters) {
        Map<String, String> parameters = new HashMap<>();
        String where = filterConditions(filters, parameters);
        
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(h) FROM HumanBeing h LEFT JOIN h.car c" + where, Long.class);
        parameters.forEach((name, value) -> query.setParameter(name, value));
        
        return query.getSingleResult();
    }

    private String filterConditions(Map<String, String> filters, Map<String, String> parameters) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String value = filter.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            String column = textColumn(filter.getKey());
            String parameter = "filter" + parameters.size();
            where.append(" AND LOCATE(:")
                    .append(parameter)
                    .append(", ")
                    .append(column)
                    .append(") > 0");
        
            parameters.put(parameter, value.toLowerCase(Locale.ROOT));
        }
        return where.toString();
    }

    private String textColumn(String column) {
        String field = switch (column) {
            case "name" -> "h.name";
            case "soundtrackName" -> "h.soundtrackName";
            case "carName" -> "c.name";
            case "carColor" -> "c.color";
            case "mood" -> "CAST(h.mood AS string)";
            case "weaponType" -> "CAST(h.weaponType AS string)";
            default -> throw new WebApplicationException(GlobalExceptionHandler.response(400, "Недопустимое строковое поле"));
        };
        return "LOWER(COALESCE(" + field + ", ''))";
    }

    public List<HumanBeing> findByCarId(long id) {
        return em.createQuery(SELECT + " WHERE c.id = :id ORDER BY h.id", HumanBeing.class)
                .setParameter("id", id)
                .getResultList();
    }

    public List<HumanBeing> findByCoordinatesId(long id) {
        return em.createQuery(SELECT + " WHERE h.coordinates.id = :id ORDER BY h.id", HumanBeing.class)
                .setParameter("id", id)
                .getResultList();
    }

    public HumanBeing findFirstByWeapon(WeaponType weapon) {
        return em.createQuery(SELECT + " WHERE h.weaponType = :weapon ORDER BY h.id", HumanBeing.class)
                .setParameter("weapon", weapon)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public HumanBeing findMinimumWaiting() {
        return em.createQuery(SELECT
                        + " WHERE h.minutesOfWaiting IS NOT NULL ORDER BY h.minutesOfWaiting, h.id",
                        HumanBeing.class)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<HumanBeing> findBySoundtrack(String substring) {
        return em.createQuery(SELECT + " WHERE LOCATE(:substring, LOWER(h.soundtrackName)) > 0 ORDER BY h.id", HumanBeing.class)
                .setParameter("substring", substring.toLowerCase(Locale.ROOT))
                .getResultList();
    }

    public List<HumanBeing> findHeroesToSadden() {
        return em.createQuery(SELECT + " WHERE h.realHero = TRUE AND (h.mood IS NULL OR h.mood <> :mood) ORDER BY h.id", HumanBeing.class)
                .setParameter("mood", Mood.SORROW)
                .getResultList();
    }

    public List<HumanBeing> findHeroesWithoutCar() {
        return em.createQuery(SELECT + " WHERE h.realHero = TRUE AND h.car IS NULL ORDER BY h.id", HumanBeing.class)
                .getResultList();
    }

    public HumanBeing findById(int id) {
        HumanBeing human = em.find(HumanBeing.class, id);
        if (human == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(404, "Персонаж не найден"));
        }
        return human;
    }

    public void save(HumanBeing human) {
        em.persist(human);
    }

    public void flush() {
        em.flush();
    }

    public void delete(HumanBeing human) {
        em.remove(human);
    }
}
