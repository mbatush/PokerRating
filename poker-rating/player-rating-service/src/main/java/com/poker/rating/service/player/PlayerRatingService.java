package com.poker.rating.service.player;

import com.poker.model.rating.PlayerRating;
import com.poker.rating.service.player.model.PlayerRatingDoc;
import com.poker.rating.service.player.model.PlayerStatisticDoc;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PlayerRatingService {

  Set<PlayerRating> getPlayerRatings(String applicationId, Set<String> userIds);

  void addPlayerRatings(
      String applicationId, String sessionId, Map<String, Long> addRatingChangeByUserId);

  Optional<PlayerRatingDoc> getPlayerRatingDoc(String applicationId, String userId);

  PlayerRatingDoc resetPlayerRating(String applicationId, String userId, long rating);

  PlayerStatisticDoc appendPlayerStatistic(PlayerStatisticDoc playerStatistic);

  Optional<PlayerStatisticDoc> getPlayerStatisticDoc(String applicationId, String userId);
}
