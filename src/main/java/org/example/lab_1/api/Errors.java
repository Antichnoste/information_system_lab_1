package org.example.lab_1.api;
import jakarta.persistence.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
@Provider
public class Errors implements ExceptionMapper<Exception> {
 public record ErrorBody(String message,Map<String,String> fields) {}
 public static Response response(int status,String message,Map<String,String> fields) {
  return Response.status(status).type(MediaType.APPLICATION_JSON).entity(new ErrorBody(message,fields)).build();
 }
 @Override public Response toResponse(Exception exception) {
  for(Throwable e=exception;e!=null;e=e.getCause()) {
   if(e instanceof ApiException a) return response(a.status,a.getMessage(),a.field==null?Map.of():Map.of(a.field,a.getMessage()));
   if(e instanceof ConstraintViolationException c) {
    Map<String,String> fields=new TreeMap<>();
    c.getConstraintViolations().forEach(v->{String path=v.getPropertyPath().toString(); fields.put(path.substring(path.lastIndexOf('.')+1),v.getMessage());});
    return response(400,"Проверьте значения полей",fields);
   }
   if(e instanceof OptimisticLockException) return response(409,"Объект уже изменён. Обновите данные.",Map.of());
   if(e instanceof SQLException sql && sql.getSQLState()!=null && sql.getSQLState().startsWith("23"))
    return response(409,"Операция нарушает ограничения данных или связей",Map.of());
  }
  if(exception instanceof WebApplicationException w) {
   int status=w.getResponse().getStatus();
   return response(status,status==404?"Объект или адрес не найден":"Некорректный запрос: проверьте формат и типы полей",Map.of());
  }
  Logger.getLogger(Errors.class.getName()).log(Level.SEVERE,"Request failed",exception);
  return response(500,"Не удалось выполнить операцию",Map.of());
 }
}
