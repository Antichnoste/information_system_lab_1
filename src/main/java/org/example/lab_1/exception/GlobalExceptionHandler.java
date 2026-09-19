package org.example.lab_1.exception;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.lab_1.dto.ErrorResponse;

// Единый формат ошибок для неправильного ввода и ошибок работы с БД.
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception>, ContainerResponseFilter {
    public static Response response(int status, String message, Map<String, String> fields) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(message, fields)).build();
    }

    @Override
    public Response toResponse(Exception exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof WebApplicationException error) {
                if (error.getResponse().hasEntity()) return error.getResponse();
                return response(error.getResponse().getStatus(), error.getMessage(), Map.of());
            }
            if (cause instanceof ConstraintViolationException error) {
                return validationResponse(error);
            }
            if (cause instanceof SQLException error && error.getSQLState() != null
                    && error.getSQLState().startsWith("23")) {
                return response(409, "Операция нарушает ограничения данных или связей", Map.of());
            }
        }
        Logger.getLogger(GlobalExceptionHandler.class.getName()).log(Level.SEVERE, "Ошибка запроса", exception);
        return response(500, "Не удалось выполнить операцию", Map.of());
    }

    static Response validationResponse(ConstraintViolationException error) {
        Map<String, String> fields = new HashMap<>();
        for (var violation : error.getConstraintViolations()) {
            String field = "body";
            for (var node : violation.getPropertyPath()) {
                if (node.getKind() == ElementKind.RETURN_VALUE) {
                    return response(500, "Не удалось выполнить операцию", Map.of());
                }
                // В пути createCar.arg0.cool клиенту нужно только имя поля cool.
                if (node.getKind() == ElementKind.PROPERTY) field = node.getName();
            }
            fields.put(field, violation.getMessage());
        }
        return response(400, "Проверьте значения полей", fields);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        // Ошибки разбора JSON могут обрабатываться самим сервером до вызова наших методов.
        if (response.getStatus() >= 400 && !(response.getEntity() instanceof ErrorResponse)) {
            response.setEntity(new ErrorResponse("Некорректный запрос: проверьте JSON, типы и значения полей", Map.of()),
                    new java.lang.annotation.Annotation[0], MediaType.APPLICATION_JSON_TYPE);
        }
    }
}
