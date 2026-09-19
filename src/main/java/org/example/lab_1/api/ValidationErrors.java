package org.example.lab_1.api;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
@Provider
public class ValidationErrors implements ExceptionMapper<ConstraintViolationException> {
 public Response toResponse(ConstraintViolationException exception) { return new Errors().toResponse(exception); }
}
