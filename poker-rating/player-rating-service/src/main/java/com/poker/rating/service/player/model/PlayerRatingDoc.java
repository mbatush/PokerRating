package com.poker.rating.service.player.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(PlayerRatingDoc.COLLECTION_NAME)
public class PlayerRatingDoc implements Persistable<String> {
  public static final String COLLECTION_NAME = "playerRatings";
  @Id private final String id;
  @Nullable private final String applicationId;
  @Nullable private final String userId;
  @Nullable private final Long rating;
  @Nullable private final Instant updatedAt;
  @Nullable private final String sessionId;
  private final NavigableSet<PlayerRatingHistory> ratingHistories;
  @Version @Nullable private final Long version;

  @PersistenceCreator
  public PlayerRatingDoc(
      String id,
      @Nullable String applicationId,
      @Nullable String userId,
      @Nullable Long rating,
      @Nullable Instant updatedAt,
      @Nullable String sessionId,
      @Nullable NavigableSet<PlayerRatingHistory> ratingHistories,
      @Nullable Long version) {
    this.id = id;
    this.applicationId = applicationId;
    this.userId = userId;
    this.rating = rating;
    this.updatedAt = updatedAt;
    this.sessionId = sessionId;
    this.ratingHistories =
        Collections.unmodifiableNavigableSet(
            Objects.requireNonNullElseGet(ratingHistories, TreeSet::new));
    this.version = version;
  }

  public PlayerRatingDoc(String id) {
    this(id, null, null, null, null, null, null, null);
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  @JsonIgnore
  public boolean isNew() {
    return version == null;
  }

  @Nullable
  public String getApplicationId() {
    return this.applicationId;
  }

  @Nullable
  public String getUserId() {
    return this.userId;
  }

  @Nullable
  public Long getRating() {
    return this.rating;
  }

  @Nullable
  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  @Nullable
  public String getSessionId() {
    return this.sessionId;
  }

  public NavigableSet<PlayerRatingHistory> getRatingHistories() {
    return this.ratingHistories;
  }

  @Nullable
  public Long getVersion() {
    return version;
  }

  public PlayerRatingDoc incrementRating(long addToRating, String newSessionId) {
    PlayerRatingHistory playerRatingHistory =
        new PlayerRatingHistory(
            Objects.requireNonNullElse(version, 0L),
            Objects.requireNonNullElseGet(updatedAt, Instant::now),
            Objects.requireNonNullElse(rating, 0L),
            sessionId);
    NavigableSet<PlayerRatingHistory> newRatingHistories = new TreeSet<>(ratingHistories);
    newRatingHistories.add(playerRatingHistory);
    return new PlayerRatingDoc(
        id,
        applicationId,
        userId,
        Objects.requireNonNullElse(rating, 0L) + addToRating,
        Instant.now(),
        newSessionId,
        newRatingHistories,
        version);
  }
}
