package org.example.lab_1.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

// Сохраняет наш формат ошибок при автоматической проверке @Valid в Jakarta REST.
@Provider
public class ConstraintViolationHandler implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return GlobalExceptionHandler.validationResponse(exception);
    }
}
