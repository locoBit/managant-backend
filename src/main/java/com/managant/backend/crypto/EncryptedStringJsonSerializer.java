package com.managant.backend.crypto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Defensive serializer: in case a ciphertext leaks into a DTO or response,
 * we don't want to ship it to clients.
 */
public class EncryptedStringJsonSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }

    // Always redact. If you need phone numbers, return the decrypted value
    // via a dedicated field (we do that in PersonDto.phoneNumber).
    gen.writeString("[encrypted]");
  }
}
