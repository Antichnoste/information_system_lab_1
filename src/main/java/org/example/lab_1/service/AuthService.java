package org.example.lab_1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import org.example.lab_1.dto.AuthRequest;
import org.example.lab_1.model.AppUser;
import org.example.lab_1.repository.AppUserRepository;
import org.example.lab_1.security.Passwords;

@ApplicationScoped
public class AuthService {
    @Inject
    private AppUserRepository appUserRepository;

    @Transactional
    public String register(AuthRequest input) {
        if (appUserRepository.findByUsername(input.getUsername()) != null) {
            throw new WebApplicationException("Этот логин уже занят", 409);
        }

        AppUser user = new AppUser();
        user.setUsername(input.getUsername());
        user.setPasswordHash(Passwords.hash(input.getPassword()));
        appUserRepository.save(user);
        return user.getUsername();
    }

    public String authenticate(AuthRequest input) {
        if (input == null || input.getUsername() == null || input.getPassword() == null) {
            throw new BadRequestException("Укажите логин и пароль");
        }

        AppUser user = appUserRepository.findByUsername(input.getUsername());
        if (user == null) {
            throw new WebApplicationException("Неверный логин или пароль", 401);
        }

        if (!Passwords.matches(input.getPassword(), user.getPasswordHash())) {
            throw new WebApplicationException("Неверный логин или пароль", 401);
        }

        return user.getUsername();
    }
}
