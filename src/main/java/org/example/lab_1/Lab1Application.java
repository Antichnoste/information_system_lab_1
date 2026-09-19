package org.example.lab_1;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import org.example.lab_1.api.AuthResource;
import org.example.lab_1.api.CatalogResource;
import org.example.lab_1.security.AuthFilter;
@ApplicationPath("/api") @ApplicationScoped
public class Lab1Application extends Application {
 @Override public Set<Class<?>> getClasses() {
  return Set.of(AuthResource.class,CatalogResource.class,AuthFilter.class);
 }
}
