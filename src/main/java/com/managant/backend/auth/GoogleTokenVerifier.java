package com.managant.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifier {

  private final String clientId;

  public GoogleTokenVerifier() {
    // Keep it simple: env var. If missing, login will fail fast.
    this.clientId = System.getenv("GOOGLE_CLIENT_ID");
  }

  public VerifiedGoogleToken verify(String idTokenString) {
    if (idTokenString == null || idTokenString.isBlank()) {
      throw new IllegalArgumentException("Token inválido");
    }

    String s = idTokenString.trim();
    // Basic sanity: JWT-ish
    if (s.chars().filter(ch -> ch == '.').count() != 2) {
      throw new IllegalArgumentException("Token inválido");
    }

    // Avoid log spam with huge strings
    if (s.length() > 4096) {
      throw new IllegalArgumentException("Token inválido");
    }

    if (clientId == null || clientId.isBlank()) {
      throw new IllegalArgumentException("GOOGLE_CLIENT_ID no configurado");
    }

    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
          .setAudience(Collections.singletonList(clientId))
          .build();

      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        throw new IllegalArgumentException("Token inválido");
      }

      var payload = idToken.getPayload();
      String email = payload.getEmail();
      Boolean emailVerified = payload.getEmailVerified();

      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Token inválido");
      }

      if (emailVerified != null && !emailVerified) {
        throw new IllegalArgumentException("Email no verificado");
      }

      return new VerifiedGoogleToken(email.toLowerCase());
    } catch (IllegalArgumentException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalArgumentException("Token inválido");
    }
  }

  public record VerifiedGoogleToken(String email) {}
}
