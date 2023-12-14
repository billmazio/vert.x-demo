package com.vas.maz.vertx_starter.user;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final DatabaseService databaseService;

  public UserService(DatabaseService databaseService) {
    this.databaseService = databaseService;
  }
  public UserService(Vertx vertx) {
    // Create an instance of DatabaseService using vertx
    this.databaseService = new DatabaseService(JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:mysql://localhost:3306/vertex")
      .put("driver_class", "com.mysql.cj.jdbc.Driver")
      .put("user", "root")
      .put("password", "ww321278?")
    ));
  }


  public void login(RoutingContext routingContext) {
    try {
      // Get the request body as JSON
      JsonObject requestBody = routingContext.getBodyAsJson();
      if (requestBody == null || requestBody.isEmpty()) {
        routingContext.fail(400); // Bad Request
        return;
      }

      // Get username and password from JSON body
      String username = requestBody.getString("username");
      String password = requestBody.getString("password");

      // Perform user authentication logic
      boolean authenticated = performAuthentication(username, password);

      // Create a new User instance without setting the id manually
      User user = new User();
      user.setUsername(username);
      user.setPassword(password);

      // Save the user to the database
      databaseService.saveUser(user, ar -> {
        if (ar.succeeded()) {
          logger.info("User saved successfully");
        } else {
          logger.error("Failed to save user", ar.cause());
        }
      });

      if (authenticated) {
        // Respond with a success message or a token
        routingContext.response().putHeader("Content-Type", "application/json").end(new JsonObject().put("message", "Login successful").encode());
        logger.info("Login successful");
      } else {
        // Respond with an authentication failure message
        routingContext.response().setStatusCode(401).end("Authentication failed");
        logger.info("Login failed - Authentication failed");
      }

    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during login", e);
    }
  }




  private boolean performAuthentication(String username, String password) {
    // Implement your authentication logic here
    // For simplicity, let's assume all logins are successful
    return true;
  }
}
