package com.example.backend.exception;

public class FoodExpiredException extends RuntimeException {
  public FoodExpiredException(String message) {
    super(message);
  }

  public FoodExpiredException(String message, Throwable cause) {
    super(message, cause);
  }
}