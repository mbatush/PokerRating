package com.poker.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SuppressWarnings("UnusedVariable")
public class RunBulk {

  public static void main(String[] args) throws Exception {
    if (args.length <= 0) {
      System.out.println("Please provide path to input file");
      System.exit(1);
      return;
    }
    Path inputPath = Paths.get(args[0]);
    if (!Files.exists(inputPath)) {
      System.out.println(
          "Please provide valid path to input file, the given path does not exist: " + args[0]);
      System.exit(1);
      return;
    }

    List<String> handsLines = Files.readAllLines(inputPath);

    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    boolean startBlock = false;
    boolean endBlock = false;
    String block = "";
    int blockCount = 0;
    for (String line : handsLines) {
      if (line.startsWith("Players")) {
        startBlock = true;
      }
      if (startBlock && line.isBlank()) {
        endBlock = true;
        startBlock = false;
      }
      if (startBlock) {
        block += line + "\n";
      }
      if (endBlock) {
        block = block.substring(0, block.length() - 1);
        blockCount++;
        HttpRequest request = postJson(block);
        Instant start = Instant.now();
        HttpResponse<String> httpResponse = httpClient.send(request, BodyHandlers.ofString());
        Instant end = Instant.now();
        String response = mapSuccessOrThrow(httpResponse, block);
        System.out.println(
            "Game hand sent: "
                + blockCount
                + ". Time seconds: "
                + end.minusSeconds(start.getEpochSecond()).getEpochSecond());
        block = "";
        endBlock = false;
      }
    }
    System.out.println("Total games: " + blockCount);
  }

  private static HttpRequest postJson(String payload) throws URISyntaxException {
    return HttpRequest.newBuilder(
            new URI("http://157.230.200.133/rule-engine/internal/rating/game/calc/text"))
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .timeout(Duration.ofMinutes(10))
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();
  }

  public static String mapSuccessOrThrow(HttpResponse<String> httpResponse, String request) {
    var statusCode = httpResponse.statusCode();
    String responseBody = httpResponse.body();
    if (statusCode < 200 || statusCode > 299) {
      throw new IllegalStateException(
          String.format("Request:%n%s%nHTTP: %d%n%s", request, statusCode, responseBody));
    }
    return responseBody;
  }
}
