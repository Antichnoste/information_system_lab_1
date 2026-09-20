package org.example.lab_1.security;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.lab_1.model.AppUser;
import org.example.lab_1.repository.AppUserRepository;

@ApplicationScoped
public class UserBootstrap {
    @Inject
    private AppUserRepository appUserRepository;

    @PostConstruct
    public void init() {
        if (appUserRepository.count() == 0) {
            appUserRepository.save(new AppUser("admin", Passwords.hash("123")));
            appUserRepository.save(new AppUser("user1", Passwords.hash("123")));
        }
    }
}
