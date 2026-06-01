package com.miasi.runner;

import io.javalin.Javalin;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public final class Main {

  private static final String PROPERTIES_RESOURCE = "application.properties";

  private Main() {}

  public static void main(String[] args) {
    Properties properties = loadMergedProperties(PROPERTIES_RESOURCE);

    int port = Integer.parseInt(properties.getProperty("server.port", "7070"));

    Javalin app = Javalin.create(config -> {
      config.routes.get("/health", ctx -> ctx.result("OK"));
    });

    app.start(port);
    System.out.println("Helpdesk app running on http://localhost:" + port);
  }

  private static Properties loadMergedProperties(String resourceName) {
    Properties merged = new Properties();
    try {
      Enumeration<URL> resources = Main.class
        .getClassLoader()
        .getResources(resourceName);
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream in = url.openStream()) {
          merged.load(in);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(
        "Failed to load properties from " + resourceName,
        e
      );
    }
    return merged;
  }
}
