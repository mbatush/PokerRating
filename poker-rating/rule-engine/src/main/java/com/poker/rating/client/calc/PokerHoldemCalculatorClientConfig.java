package com.poker.rating.client.calc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "poker.holdem.calc.client")
public record PokerHoldemCalculatorClientConfig(
    @Nonnull String endpoint,
    @Nullable Integer connectTimeoutSeconds,
    @Nullable Integer readTimeoutSeconds) {}
