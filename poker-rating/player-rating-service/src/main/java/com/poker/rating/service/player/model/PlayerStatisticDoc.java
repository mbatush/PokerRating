package com.poker.rating.service.player.model;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.poker.util.FluentUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

@SuppressWarnings({"SameNameButDifferent", "MissingSummary", "MissingOverride"})
@Document(PlayerStatisticDoc.COLLECTION_NAME)
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor = @__({@PersistenceCreator}))
@Jacksonized
@Getter
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  "applicationId",
  "userId",
  "version",
  "totalPositiveFinalSumDecisions",
  "totalNegativeFinalSumDecisions",
  "maxPositiveSumFinalDecisions",
  "maxNegativeSumFinalDecisions",
  "totalPositiveDecisions",
  "totalNegativeDecisions",
  "totalPositiveDecisionsPerRound",
  "totalNegativeDecisionsPerRound",
  "totalRulesCount"
})
public class PlayerStatisticDoc implements Persistable<String> {
  public static final String COLLECTION_NAME = "playerStatistics";

  @Id @Nonnull @JsonIgnore private final String id;

  @Nullable private final String applicationId;
  @Nullable private final String userId;
  @Version @Nullable private final Long version;
  @Nullable private final Long totalPositiveFinalSumDecisions;
  @Nullable private final Long totalNegativeFinalSumDecisions;
  @Nullable private final Long maxPositiveSumFinalDecisions;
  @Nullable private final Long maxNegativeSumFinalDecisions;
  @Nullable private final Long totalPositiveDecisions;
  @Nullable private final Long totalNegativeDecisions;

  @Nullable private final Map<String, Long> totalPositiveDecisionsPerRound;

  @Nullable private final Map<String, Long> totalNegativeDecisionsPerRound;

  @Nullable private final Map<String, Map<String, Long>> totalRulesCount;

  public static String toDocId(String applicationId, String userId) {
    return "urn:applicationId:" + applicationId + ":userId:" + userId;
  }

  private static long requireNonNullOrZero(@Nullable Long number) {
    return requireNonNullElse(number, 0L);
  }

  private static long safeSum(@Nullable Long number1, @Nullable Long number2) {
    long x = requireNonNullOrZero(number1);
    long y = requireNonNullOrZero(number2);
    long sum = x + y;
    // see Math.addExact()
    if (((x ^ sum) & (y ^ sum)) < 0) {
      return Long.MAX_VALUE;
    }
    return sum;
  }

  private static long safeMax(@Nullable Long number1, @Nullable Long number2) {
    long x = requireNonNullOrZero(number1);
    long y = requireNonNullOrZero(number2);
    return Math.max(x, y);
  }

  public PlayerStatisticDoc append(PlayerStatisticDoc playerStatistic) {
    if (FluentUtils.not(Objects.equals(this.id, playerStatistic.getId()))) {
      throw new IllegalArgumentException(
          String.format(
              "Could not append player statistic data on different id given '%s'. Expected is: '%s'",
              playerStatistic.getId(), this.id));
    }
    PlayerStatisticDocBuilder builder =
        PlayerStatisticDoc.builder()
            .id(this.id)
            .version(version)
            .applicationId(this.applicationId)
            .userId(this.userId)
            .totalPositiveFinalSumDecisions(
                safeSum(
                    this.totalPositiveFinalSumDecisions,
                    playerStatistic.getTotalPositiveFinalSumDecisions()))
            .totalNegativeFinalSumDecisions(
                safeSum(
                    this.totalNegativeFinalSumDecisions,
                    playerStatistic.getTotalNegativeFinalSumDecisions()))
            .maxPositiveSumFinalDecisions(
                safeMax(
                    this.maxPositiveSumFinalDecisions,
                    playerStatistic.getMaxPositiveSumFinalDecisions()))
            .maxNegativeSumFinalDecisions(
                safeMax(
                    this.maxNegativeSumFinalDecisions,
                    playerStatistic.getMaxNegativeSumFinalDecisions()))
            .totalPositiveDecisions(
                safeSum(this.totalPositiveDecisions, playerStatistic.getTotalPositiveDecisions()))
            .totalNegativeDecisions(
                safeSum(this.totalNegativeDecisions, playerStatistic.getTotalNegativeDecisions()));

    Map<String, Map<String, Long>> finalTotalRulesCount = appendTotalRulesCount(playerStatistic);
    builder.totalRulesCount(finalTotalRulesCount);
    builder.totalPositiveDecisionsPerRound(appendTotalPositiveDecisionsPerRound(playerStatistic));
    builder.totalNegativeDecisionsPerRound(appendTotalNegativeDecisionsPerRound(playerStatistic));
    return builder.build();
  }

  Map<String, Map<String, Long>> appendTotalRulesCount(PlayerStatisticDoc playerStatistic) {
    Map<String, Map<String, Long>> finalTotalRulesCount = new HashMap<>();
    Map<String, Map<String, Long>> currentTotalRulesCount =
        requireNonNullElseGet(this.totalRulesCount, HashMap::new);
    Map<String, Map<String, Long>> newTotalRulesCount =
        requireNonNullElseGet(playerStatistic.getTotalRulesCount(), HashMap::new);
    currentTotalRulesCount.forEach(
        (k, v) -> {
          Map<String, Long> newFirstLevelValue = newTotalRulesCount.get(k);
          if (newFirstLevelValue == null) {
            finalTotalRulesCount.put(k, v);
          } else {
            v.forEach(
                (k1, v1) -> {
                  Long newValue = newFirstLevelValue.get(k1);
                  finalTotalRulesCount
                      .computeIfAbsent(k, key -> new HashMap<>())
                      .put(k1, safeSum(v1, newValue));
                });
          }
        });
    newTotalRulesCount.forEach(
        (k, v) -> {
          if (FluentUtils.not(currentTotalRulesCount.containsKey(k))) {
            finalTotalRulesCount.put(k, v);
          }
        });
    return finalTotalRulesCount;
  }

  Map<String, Long> appendTotalPositiveDecisionsPerRound(PlayerStatisticDoc playerStatistic) {
    return appendTotalLongsPerRound(
        this.totalPositiveDecisionsPerRound, playerStatistic.getTotalPositiveDecisionsPerRound());
  }

  Map<String, Long> appendTotalNegativeDecisionsPerRound(PlayerStatisticDoc playerStatistic) {
    return appendTotalLongsPerRound(
        this.totalNegativeDecisionsPerRound, playerStatistic.getTotalNegativeDecisionsPerRound());
  }

  Map<String, Long> appendTotalLongsPerRound(
      @Nullable Map<String, Long> current, @Nullable Map<String, Long> other) {
    Map<String, Long> finalTotalPerRound = new HashMap<>();
    Map<String, Long> currentOrEmpty = requireNonNullElseGet(current, HashMap::new);
    Map<String, Long> otherOrEmpty = requireNonNullElseGet(other, HashMap::new);
    currentOrEmpty.forEach((k, v) -> finalTotalPerRound.put(k, safeSum(v, otherOrEmpty.get(k))));
    otherOrEmpty.forEach(
        (k, v) -> {
          if (FluentUtils.not(currentOrEmpty.containsKey(k))) {
            finalTotalPerRound.put(k, v);
          }
        });
    return finalTotalPerRound;
  }

  @Override
  @JsonIgnore
  public boolean isNew() {
    return version == null;
  }
}
