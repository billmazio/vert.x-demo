package com.vas.maz.vertx_starter;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.DriverManager;

@ExtendWith(VertxExtension.class)
public class TestMainAppVerticle {
  public static void main(String[] args) {
    String url = "jdbc:mysql://localhost:3306/vertx";
    String user = "root";
    String password = "ww321278?";

    try {
      Connection connection = DriverManager.getConnection(url, user, password);
      System.out.println("Connection successful");
      connection.close();
    } catch (Exception e) {
      System.err.println("Connection failed: " + e.getMessage());
    }
  }

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
}
