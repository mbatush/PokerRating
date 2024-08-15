package com.poker.test.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileTestUtils {

  private FileTestUtils() {
    throw new UnsupportedOperationException("No instance for util class");
  }

  public static String readUtf8Content(String classpathResource) {
    var classpathResourceUrl = FileTestUtils.class.getClassLoader().getResource(classpathResource);
    if (classpathResourceUrl == null) {
      throw new UncheckedIOException(
          new FileNotFoundException("Classpath resource not found: " + classpathResource));
    }

    try {
      return Files.readString(Path.of(classpathResourceUrl.toURI()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
