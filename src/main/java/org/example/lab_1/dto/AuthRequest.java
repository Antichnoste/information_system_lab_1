package org.example.lab_1.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
}
