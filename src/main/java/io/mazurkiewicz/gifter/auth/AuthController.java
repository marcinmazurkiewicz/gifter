package io.mazurkiewicz.gifter.auth;

import io.mazurkiewicz.gifter.auth.AuthService.Token;
import io.mazurkiewicz.gifter.user.PasswordsNotEqualException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    ResponseEntity<LoginUserResponse> registerNewUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        if (!registerUserRequest.password().equals(registerUserRequest.confirmPassword())) {
            throw new PasswordsNotEqualException();
        }
        Token token = authService.registerUser(registerUserRequest.firstName(), registerUserRequest.lastName(),
                registerUserRequest.email(), registerUserRequest.password());

        return ResponseEntity.ok(new LoginUserResponse(token.accessToken(), token.refreshToken()));
    }

    @PostMapping("/auth/login")
    ResponseEntity<LoginUserResponse> loginUser(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        Token token = authService.loginUser(loginUserRequest.email(), loginUserRequest.password());
        return ResponseEntity.ok(new LoginUserResponse(token.accessToken(), token.refreshToken()));
    }

    @PostMapping("/auth/logout")
    ResponseEntity<Void> logoutUser(@Valid @RequestBody LogoutUserRequest logoutUserRequest) {
        authService.logoutUser(logoutUserRequest.refreshToken());
        return ResponseEntity.ok().build();
    }

    record RegisterUserRequest(@NotBlank String firstName,
                               @NotBlank String lastName,
                               @NotBlank @Email String email,
                               @NotBlank String password,
                               @NotBlank String confirmPassword) {
    }

    record LoginUserRequest(@NotBlank String email, @NotBlank String password) {
    }

    record LoginUserResponse(String accessToken, String refreshToken) {
    }

    record LogoutUserRequest(@NotBlank String refreshToken) {
    }
}
