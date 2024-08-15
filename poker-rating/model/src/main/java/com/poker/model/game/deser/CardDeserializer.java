package com.poker.model.game.deser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.poker.model.game.Card;
import java.io.IOException;

public class CardDeserializer extends StdDeserializer<Card> {

  protected CardDeserializer() {
    super(Card.class);
  }

  @Override
  public Card deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {
    return Card.of(_parseString(p, ctxt));
  }

  @Override
  public Card getNullValue(DeserializationContext ctxt) throws JsonMappingException {
    return Card.of(null);
  }
}
