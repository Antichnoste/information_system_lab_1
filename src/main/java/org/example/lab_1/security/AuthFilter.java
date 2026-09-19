package org.example.lab_1.security;
import jakarta.annotation.Priority;
import jakarta.servlet.http.*;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.util.*;
import org.example.lab_1.api.Errors;
@Provider @Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter,ContainerResponseFilter {
 @Context HttpServletRequest request;
 public void filter(ContainerRequestContext context) {
  String path=context.getUriInfo().getPath().replaceFirst("^/+", "");
  boolean mutation=!Set.of("GET","HEAD","OPTIONS").contains(context.getMethod());
  if(mutation && !"HumanBeing".equals(context.getHeaderString("X-Requested-With"))) {
   context.abortWith(Errors.response(403,"Отсутствует заголовок X-Requested-With",Map.of())); return;
  }
  if(path.equals("health") || path.equals("auth/login")) return;
  HttpSession session=request.getSession(false);
  if(session==null || session.getAttribute("username")==null) {
   context.abortWith(Errors.response(401,"Войдите в систему",Map.of())); return;
  }
  if(mutation && !Objects.equals(session.getAttribute("csrf"),context.getHeaderString("X-CSRF-Token")))
   context.abortWith(Errors.response(403,"Сессия устарела. Обновите страницу.",Map.of()));
 }
 public void filter(ContainerRequestContext req,ContainerResponseContext res) {
  if(res.getStatus()>=400 && !(res.getEntity() instanceof Errors.ErrorBody)) {
   res.setEntity(new Errors.ErrorBody("Некорректный запрос: проверьте JSON, типы и значения полей",Map.of()),
    new java.lang.annotation.Annotation[0],jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE);
  }
  res.getHeaders().putSingle("Cache-Control","no-store");
  res.getHeaders().putSingle("X-Content-Type-Options","nosniff");
 }
}
