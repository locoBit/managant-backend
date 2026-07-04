package com.managant.backend.domain;

import java.time.LocalDate;

public final class CurpVariant {
  private CurpVariant() {}

  public static String generate(String firstNames, String lastName, String motherLastName, LocalDate birthDate) {
    String f = clean(firstNames).substring(0, Math.min(2, clean(firstNames).length()));
    String p = clean(lastName).substring(0, Math.min(2, clean(lastName).length()));
    String m = clean(motherLastName).substring(0, Math.min(2, clean(motherLastName).length()));

    String date = birthDate != null ? birthDate.toString().replace("-", "") : "";
    return (p + m + f + date);
  }

  private static String clean(String s) {
    if (s == null) return "";
    return s.trim().toUpperCase().replaceAll("[^A-Z]", "");
  }
}
