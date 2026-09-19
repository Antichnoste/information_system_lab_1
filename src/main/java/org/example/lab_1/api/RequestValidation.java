package org.example.lab_1.api;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
@ApplicationScoped
public class RequestValidation {
 @Inject Validator validator;
 public <T> T check(T input) {
  if(input==null) throw new ApiException(400,"Тело запроса обязательно");
  var violations=validator.validate(input);
  if(!violations.isEmpty()) throw new ConstraintViolationException(violations);
  return input;
 }
}
