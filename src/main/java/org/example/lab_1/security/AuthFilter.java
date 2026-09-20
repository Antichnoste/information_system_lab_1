package org.example.lab_1.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import org.example.lab_1.exception.GlobalExceptionHandler;

// Для всех запросов, кроме входа и регистрации, проверяем наличие обычной HTTP-сессии.
@Provider
public class AuthFilter implements ContainerRequestFilter {
    @Context
    HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext context) {
        String path = context.getUriInfo().getPath().replaceFirst("^/+", "");
        if (path.equals("auth/login") || path.equals("auth/register")) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            context.abortWith(GlobalExceptionHandler.response(401, "Войдите в систему", Map.of()));
        }
    }
}
