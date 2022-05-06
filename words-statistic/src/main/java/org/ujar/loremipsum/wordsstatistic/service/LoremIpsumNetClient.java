package org.ujar.loremipsum.wordsstatistic.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.ujar.loremipsum.wordsstatistic.config.NetClientProperties;
import org.ujar.loremipsum.wordsstatistic.enums.LengthType;
import org.ujar.loremipsum.wordsstatistic.exception.NetClientCommunicationException;
import org.ujar.loremipsum.wordsstatistic.exception.NetClientMisconfigurationException;

@Service
public class LoremIpsumNetClient {
  private final NetClientProperties properties;
  private final HttpClient httpClient;

  public LoremIpsumNetClient(NetClientProperties properties) {
    this.properties = properties;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
        .build();
  }

  public String getText(Integer paragraphsNum, LengthType lengthType) {
    URI uri;
    try {
      uri = new URI(properties.getApiServerUrlTemplate()
          .replace("{paragraphsNum}", paragraphsNum.toString())
          .replace("{lengthType}", lengthType.getType()));
    } catch (URISyntaxException e) {
      throw new NetClientMisconfigurationException(e);
    }
    var request = HttpRequest.newBuilder()
        .uri(uri)
        .headers("Content-Type", "text/plain; charset=utf-8")
        .timeout(Duration.ofSeconds(properties.getRequestTimeout()))
        .GET()
        .build();
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    } catch (IOException | InterruptedException e) {
      throw new NetClientCommunicationException(e);
    }
  }
}
