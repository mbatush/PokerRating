package com.poker.util.jdk.http;

import lombok.Getter;

@SuppressWarnings("SameNameButDifferent")
@Getter
public class HttpClientException extends RuntimeException {

  private final int statusCode;
  private final String responseBody;

  public HttpClientException(int statusCode, String responseBody) {
    super("HTTP " + statusCode + "." + responseBody);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }
}
