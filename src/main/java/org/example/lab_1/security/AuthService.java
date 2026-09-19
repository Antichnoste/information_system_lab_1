package org.example.lab_1.security;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.lab_1.api.ApiException;
import org.example.lab_1.model.AppUser;
import org.example.lab_1.repository.CatalogRepository;
@ApplicationScoped
public class AuthService {
 @Inject CatalogRepository repo;
 private final String dummy=Passwords.hash("nonexistent-account");
 @Transactional public String login(String username,String password) {
  var users=repo.query("from AppUser where username=:username",AppUser.class).setParameter("username",username).getResultList();
  boolean valid=Passwords.verify(password,users.isEmpty()?dummy:users.get(0).passwordHash);
  if(users.isEmpty() || !valid) throw new ApiException(401,"Неверный логин или пароль");
  return users.get(0).username;
 }
}
