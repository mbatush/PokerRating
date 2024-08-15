package com.poker.rating.service;

import javax.annotation.Nonnull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "poker.percentage.calc")
public record PokerPercentageCalculatorConfig(@Nonnull int showdownCalcParallelism) {}
