package com.poker.rating.client.calc;

import static java.util.Objects.requireNonNullElse;

import com.poker.rating.client.calc.model.ShowdownPercentageRequest;
import com.poker.rating.client.calc.model.ShowdownPercentageResponse;
import com.poker.rating.client.calc.model.WinPercentageRequest;
import com.poker.rating.client.calc.model.WinPercentageResponse;
import com.poker.util.jdk.http.JdkHttpClientUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@SuppressWarnings("SameNameButDifferent")
public class HttpJdkPokerHoldemCalculatorClient implements PokerHoldemCalculatorClient {

  @Nonnull private final HttpClient httpClient;
  @Nonnull private final PokerHoldemCalculatorClientConfig config;

  public HttpJdkPokerHoldemCalculatorClient(PokerHoldemCalculatorClientConfig config) {
    this.config = config;
    this.httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout(config)).build();
  }

  @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 500))
  @SneakyThrows
  @Override
  public WinPercentageResponse winPercentage(WinPercentageRequest winPercentageRequest) {
    HttpRequest request = postJson(WIN_PERCENTAGE_RESOURCE, winPercentageRequest);
    HttpResponse<String> httpResponse = httpClient.send(request, BodyHandlers.ofString());
    return JdkHttpClientUtils.mapSuccessOrThrow(httpResponse, WinPercentageResponse.class);
  }

  @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 500))
  @SneakyThrows
  @Override
  public ShowdownPercentageResponse showdownPercentage(
      ShowdownPercentageRequest showdownPercentageRequest) {
    HttpRequest request = postJson(SHOWDOWN_PERCENTAGE_RESOURCE, showdownPercentageRequest);
    HttpResponse<String> httpResponse = httpClient.send(request, BodyHandlers.ofString());
    return JdkHttpClientUtils.mapSuccessOrThrow(httpResponse, ShowdownPercentageResponse.class);
  }

  private HttpRequest postJson(String resource, Object request) throws URISyntaxException {
    return HttpRequest.newBuilder(new URI(config.endpoint() + resource))
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .timeout(readTimeout())
        .POST(JdkHttpClientUtils.jacksonBodyPublisher(request))
        .build();
  }

  private Duration connectTimeout(PokerHoldemCalculatorClientConfig config) {
    return Duration.ofSeconds(requireNonNullElse(config.connectTimeoutSeconds(), 10));
  }

  private Duration readTimeout() {
    return Duration.ofSeconds(requireNonNullElse(config.readTimeoutSeconds(), 120));
  }
}
