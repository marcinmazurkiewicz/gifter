package io.mazurkiewicz.gifter.auth;

public interface AuthService {

    Token loginUser(String email, String password);

    void logoutUser(String token);

    Token registerUser(String name, String lastname, String email, String password);

    record Token(String accessToken, String refreshToken) {
    }
}
