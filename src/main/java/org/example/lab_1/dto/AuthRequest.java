package org.example.lab_1.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Укажите логин")
    @Size(max = 255, message = "Логин должен содержать не больше 255 символов")
    private String username;
    @NotBlank(message = "Укажите пароль")
    private String password;
}
