package com.managant.backend.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

  // GCM recommended IV length
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;

  private final SecureRandom secureRandom = new SecureRandom();
  private final byte[] key;

  public CryptoService(@Value("${app.crypto.phone-key:}") String base64Key) {
    if (base64Key == null || base64Key.trim().isEmpty()) {
      this.key = null;
      return;
    }

    try {
      this.key = Base64.getDecoder().decode(base64Key.trim());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "PHONE_ENCRYPTION_KEY is not valid base64. Generate one with: openssl rand -base64 32"
      );
    }

    if (this.key.length != 32) {
      throw new IllegalArgumentException(
          "PHONE_ENCRYPTION_KEY must be base64 for exactly 32 bytes (AES-256-GCM)"
      );
    }
  }

  public boolean isConfigured() {
    return key != null;
  }

  public String encryptToBase64(String plaintext) {
    if (plaintext == null || plaintext.isBlank()) return null;
    requireConfigured();

    try {
      byte[] iv = new byte[IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      var keySpec = new SecretKeySpec(key, "AES");
      var gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      // Store as: iv || ciphertext (ciphertext already includes auth tag at the end)
      byte[] out = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);

      return Base64.getEncoder().encodeToString(out);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to encrypt phone", ex);
    }
  }

  public String decryptFromBase64(String encoded) {
    if (encoded == null || encoded.isBlank()) return null;
    requireConfigured();

    try {
      byte[] in = Base64.getDecoder().decode(encoded.trim());
      if (in.length < IV_LENGTH_BYTES + 1) {
        throw new IllegalArgumentException("Invalid ciphertext");
      }

      byte[] iv = new byte[IV_LENGTH_BYTES];
      byte[] ciphertext = new byte[in.length - IV_LENGTH_BYTES];
      System.arraycopy(in, 0, iv, 0, IV_LENGTH_BYTES);
      System.arraycopy(in, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      var keySpec = new SecretKeySpec(key, "AES");
      var gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to decrypt phone", ex);
    }
  }

  private void requireConfigured() {
    if (!isConfigured()) {
      throw new IllegalStateException(
          "Phone encryption key not configured. Set PHONE_ENCRYPTION_KEY env var."
      );
    }
  }
}
