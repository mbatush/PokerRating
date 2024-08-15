package com.poker.rating.rest.internal.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class PlayerStatisticsDocNotFoundProblem extends AbstractThrowableProblem {

  public PlayerStatisticsDocNotFoundProblem(String applicationId, String userId) {
    super(
        Problem.DEFAULT_TYPE,
        "Not Found",
        Status.NOT_FOUND,
        String.format(
            "Player statistics '%s' not found on application '%s'", userId, applicationId));
  }
}
