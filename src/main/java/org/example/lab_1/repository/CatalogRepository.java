package org.example.lab_1.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import java.util.*;
import org.example.lab_1.model.*;
import org.example.lab_1.api.ApiException;
@ApplicationScoped
public class CatalogRepository {
 @PersistenceContext(unitName="humanity") EntityManager em;
 public void lockWrites() { em.find(WriteGuard.class,1,LockModeType.PESSIMISTIC_WRITE); }
 public <T> T get(Class<T> type,Object id) {
  T entity=em.find(type,id);
  if(entity==null) throw new ApiException(404,"Объект не найден");
  return entity;
 }
 public void save(Object entity) { em.persist(entity); }
 public void remove(Object entity) { em.remove(entity); }
 public void flush() { em.flush(); }
 public <T> List<T> all(Class<T> type) { return em.createQuery("from "+type.getSimpleName()+" order by id",type).getResultList(); }
 public <T> TypedQuery<T> query(String jpql,Class<T> type) { return em.createQuery(jpql,type); }
 public static String containsPattern(String value) {
  return "%"+value.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
 }
}
