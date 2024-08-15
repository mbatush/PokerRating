package com.poker.rating.service.player;

import com.poker.rating.service.player.repo.PlayerRatingRepo;
import com.poker.rating.service.player.repo.PlayerStatisticRepo;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration(proxyBeanMethods = false)
public class PlayerRatingServiceConfig {

  @Bean
  public PlayerRatingService playerRatingService(
      MongoTemplate mongoTemplate,
      PlayerRatingRepo playerRatingRepo,
      PlayerStatisticRepo playerStatisticRepo) {
    return new MongodbPlayerRatingService(mongoTemplate, playerRatingRepo, playerStatisticRepo);
  }

  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(List.of());
  }
}
