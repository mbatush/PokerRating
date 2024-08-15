package com.poker.rating.service.player.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nullable;

public class PlayerRatingHistory implements Comparable<PlayerRatingHistory> {
  private final Long version;
  private final Instant updatedAt;
  private final Long rating;
  @Nullable private final String sessionId;

  public PlayerRatingHistory(
      Long version, Instant updatedAt, Long rating, @Nullable String sessionId) {
    this.version = version;
    this.updatedAt = updatedAt;
    this.rating = rating;
    this.sessionId = sessionId;
  }

  public Long getVersion() {
    return version;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Long getRating() {
    return rating;
  }

  @Nullable
  public String getSessionId() {
    return sessionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PlayerRatingHistory)) {
      return false;
    }
    PlayerRatingHistory that = (PlayerRatingHistory) o;
    return Objects.equals(version, that.version) && Objects.equals(updatedAt, that.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, updatedAt);
  }

  @Override
  public int compareTo(PlayerRatingHistory o) {
    return Comparator.comparing(PlayerRatingHistory::getVersion).reversed().compare(this, o);
  }

  @Override
  public String toString() {
    return "PlayerRatingHistory{"
        + "version="
        + version
        + ", updatedAt="
        + updatedAt
        + ", rating="
        + rating
        + ", sessionId='"
        + sessionId
        + '\''
        + '}';
  }
}
