package org.example.lab_1.security;

import org.mindrot.jbcrypt.BCrypt;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
public final class Passwords {
    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, storedHash);
    }
}
