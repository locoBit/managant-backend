package com.managant.backend.auth;

import com.managant.backend.auth.dto.LoginRequest;
import com.managant.backend.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final GoogleTokenVerifier googleTokenVerifier;

  public AuthController(AuthService authService, GoogleTokenVerifier googleTokenVerifier) {
    this.authService = authService;
    this.googleTokenVerifier = googleTokenVerifier;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    var verified = googleTokenVerifier.verify(request.idToken());
    return authService.loginWithGoogle(verified.email());
  }

  @PostMapping("/logout")
  public Map<String, Object> logout(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      TokenService tokenService
  ) {
    // Simple bearer token revoke.
    if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
      String token = authorization.substring(7).trim();
      if (!token.isEmpty()) tokenService.revokeToken(token);
    }

    return Map.of("success", true);
  }
}
