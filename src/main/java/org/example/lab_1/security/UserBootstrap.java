package org.example.lab_1.security;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.example.lab_1.repository.CatalogRepository;
import org.example.lab_1.model.AppUser;
@Singleton @Startup
public class UserBootstrap {
 @Inject CatalogRepository repo;
 @PostConstruct void init() {
  String users=System.getenv("APP_USERS");
  if(users==null || users.isBlank()) throw new IllegalStateException("APP_USERS must contain username:password entries");
  repo.lockWrites();
  for(String entry:users.split(",")) {
   String[] parts=entry.split(":",2);
   if(parts.length!=2 || parts[0].isBlank() || parts[1].length()<8) throw new IllegalStateException("APP_USERS: use username:password, password at least 8 characters");
   if(repo.query("from AppUser where username=:username",AppUser.class).setParameter("username",parts[0]).getResultList().isEmpty()) {
    var u=new AppUser(); u.username=parts[0]; u.passwordHash=Passwords.hash(parts[1]); repo.save(u);
   }
  }
 }
}
