package com.managant.backend.api.dto;

public record SuccessDto(boolean success) {
  public static SuccessDto ok() {
    return new SuccessDto(true);
  }
}
