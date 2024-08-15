package com.poker.util;

import java.util.List;

public final class CollectionUtils {

  private CollectionUtils() {
    throw new UnsupportedOperationException("No constructor");
  }

  public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
    int size = list.size();
    if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
      return List.of();
    }
    fromIndex = Math.max(0, fromIndex);
    toIndex = Math.min(size, toIndex);
    return list.subList(fromIndex, toIndex);
  }
}
