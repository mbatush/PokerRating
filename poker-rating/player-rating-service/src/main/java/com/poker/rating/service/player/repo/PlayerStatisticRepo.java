package com.poker.rating.service.player.repo;

import com.poker.rating.service.player.model.PlayerStatisticDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerStatisticRepo extends MongoRepository<PlayerStatisticDoc, String> {}
