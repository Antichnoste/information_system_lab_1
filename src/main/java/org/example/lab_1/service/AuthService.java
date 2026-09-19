package org.example.lab_1.service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import org.example.lab_1.dto.AuthRequest;

@ApplicationScoped
public class AuthService {
    public String authenticate(AuthRequest input) {
        if (input == null) throw new BadRequestException("Укажите логин и пароль");
        String users = System.getenv("APP_USERS");
        if (users == null) users = "alice:alice-lab-2026,bob:bob-lab-2026";

        // Учебные учётные записи заданы как логин:пароль через запятую.
        for (String entry : users.split(",")) {
            String[] user = entry.split(":", 2);
            if (user.length == 2 && user[0].equals(input.getUsername()) && user[1].equals(input.getPassword())) {
                return input.getUsername();
            }
        }
        throw new WebApplicationException("Неверный логин или пароль", 401);
    }
}
