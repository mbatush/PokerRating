package com.poker.util;

public final class FluentUtils {

  private FluentUtils() {
    throw new UnsupportedOperationException("No constructor");
  }

  public static boolean not(boolean value) {
    return !value;
  }
}
