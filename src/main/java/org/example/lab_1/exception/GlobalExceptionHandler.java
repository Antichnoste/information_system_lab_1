package org.example.lab_1.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.ValidationException;
import jakarta.validation.Path.Node;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {

            if (cause instanceof OptimisticLockException) {
                return response(409, "Объект уже изменён или удалён другим пользователем. Закройте форму и откройте её заново.");
            }

            if (cause instanceof ConstraintViolationException error) {
                List<String> messages = new ArrayList<>();
                for (ConstraintViolation<?> violation : error.getConstraintViolations()) {
                    for (Node node : violation.getPropertyPath()) {
                        if (node.getKind() == ElementKind.RETURN_VALUE) {
                            return serverError(exception);
                        }
                    }
                    messages.add(violation.getMessage());
                }

                String message = messages.isEmpty() ? "Проверьте значения полей" : String.join("; ", messages);
                return response(400, message);
            }

            if (cause instanceof ValidationException) {
                return serverError(exception);
            }

            if (cause instanceof WebApplicationException error) {
                int status = error.getResponse().getStatus();

                if (status >= 500) {
                    return serverError(exception);
                }

                if (error.getResponse().getEntity() instanceof Map<?, ?> body 
                    && body.size() == 1 
                    && body.get("message") instanceof String message) {

                    return response(status, message);
                }

                String message = switch (status) {
                    case 400 -> "Проверьте параметры запроса";
                    case 401 -> "Войдите в систему";
                    case 403 -> "Недостаточно прав";
                    case 404 -> "Запрошенный объект или адрес не найден";
                    case 405 -> "Метод запроса не поддерживается";
                    case 415 -> "Отправьте данные в формате JSON";
                    default -> "Не удалось выполнить запрос";
                };

                return Response.fromResponse(error.getResponse())
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("message", message))
                        .build();
            }

            if (cause instanceof SQLException) {
                return response(409,"Операция нарушает ограничения данных или связей");
            }
        }

        return serverError(exception);
    }

    public static Response response(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", message))
                .build();
    }

    private Response serverError(Exception exception) {
        LOG.log(Level.SEVERE, "Ошибка выполнения запроса", exception);
        return response(500,"Не удалось выполнить операцию. Попробуйте позже.");
    }
}
