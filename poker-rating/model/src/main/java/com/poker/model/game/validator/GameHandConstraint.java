package com.poker.model.game.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GameHandValidator.class)
public @interface GameHandConstraint {
  String message() default "Game hand validation failed";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
