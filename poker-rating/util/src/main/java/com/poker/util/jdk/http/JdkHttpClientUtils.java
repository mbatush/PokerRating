package com.poker.util.jdk.http;

import com.poker.util.json.JacksonUtils;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;

public class JdkHttpClientUtils {

  private JdkHttpClientUtils() {
    throw new UnsupportedOperationException("No instance for util class");
  }

  public static <T> T mapSuccessOrThrow(HttpResponse<String> httpResponse, Class<T> valueType) {
    var statusCode = httpResponse.statusCode();
    String responseBody = httpResponse.body();
    if (statusCode < 200 || statusCode > 299) {
      throw new HttpClientException(statusCode, responseBody);
    }
    return JacksonUtils.fromJson(responseBody, valueType);
  }

  public static BodyPublisher jacksonBodyPublisher(Object value) {
    return HttpRequest.BodyPublishers.ofString(JacksonUtils.toJson(value));
  }
}
