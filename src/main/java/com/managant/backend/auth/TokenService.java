package com.managant.backend.auth;

import com.managant.backend.db.SessionTokenEntity;
import com.managant.backend.db.SessionTokenRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private static final SecureRandom RNG = new SecureRandom();
  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private final SessionTokenRepository sessionRepo;

  public TokenService(SessionTokenRepository sessionRepo) {
    this.sessionRepo = sessionRepo;
  }

  public String issueToken(Long userId) {
    return issueToken(userId, 12);
  }

  public String issueToken(Long userId, int hours) {
    String token = randomHex(32); // 64 chars

    var now = LocalDateTime.now();
    var session = new SessionTokenEntity();
    session.setUserId(userId);
    session.setToken(token);
    session.setCreatedAt(now);
    session.setExpiresAt(now.plusHours(hours));
    session.setActive(true);

    sessionRepo.save(session);
    return token;
  }

  public void revokeToken(String token) {
    sessionRepo.findByTokenAndActiveTrue(token).ifPresent(s -> {
      s.setActive(false);
      sessionRepo.save(s);
    });
  }

  private static String randomHex(int bytes) {
    byte[] buf = new byte[bytes];
    RNG.nextBytes(buf);
    char[] out = new char[bytes * 2];
    for (int i = 0; i < bytes; i++) {
      int v = buf[i] & 0xFF;
      out[i * 2] = HEX[v >>> 4];
      out[i * 2 + 1] = HEX[v & 0x0F];
    }
    return new String(out);
  }
}
