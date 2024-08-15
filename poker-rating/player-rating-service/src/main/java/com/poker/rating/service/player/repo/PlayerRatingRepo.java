package com.poker.rating.service.player.repo;

import com.poker.rating.service.player.model.PlayerRatingDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRatingRepo extends MongoRepository<PlayerRatingDoc, String> {}
