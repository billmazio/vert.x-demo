package com.vas.maz.vertx_starter.user.service;

import com.vas.maz.vertx_starter.user.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private DatabaseService databaseService;


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

      // Check if the username exists in the database
      databaseService.doesUsernameExist(username, usernameExistenceResult -> {
        if (usernameExistenceResult.succeeded()) {
          boolean usernameExists = usernameExistenceResult.result();
          if (usernameExists) {
            // Username exists, proceed with authentication
            performAuthentication(username, password, authResult -> {
              if (authResult.succeeded() && authResult.result()) {
                // Authentication succeeded
                routingContext.response()
                  .putHeader("Content-Type", "application/json")
                  .putHeader("Location", "/users")
                  .setStatusCode(303) // See Other
                  .end();
                logger.info("Login successful");
              } else {
                // Authentication failed
                routingContext.response().setStatusCode(401).end(Json.encode(new JsonObject().put("success", false).put("message", "Authentication failed")));
                //        routingContext.response().setStatusCode(401).end("Authentication failed");
                logger.info("Login failed - Authentication failed");
              }
            });
          } else {
            // Username does not exist
            routingContext.response().setStatusCode(404).end("User not found");
            logger.info("Login failed - User not found");
          }
        } else {
          // Handle the result of the doesUsernameExist query failure
          routingContext.fail(500, usernameExistenceResult.cause());
          logger.error("Error checking username existence", usernameExistenceResult.cause());
        }
      });
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during login", e);
    }
  }


  private void performAuthentication(String username, String password, Handler<AsyncResult<Boolean>> resultHandler) {
    // Implement your authentication logic here
    // For simplicity, let's assume success for any non-empty username and password
    boolean authenticationSuccess = username != null && !username.isEmpty() &&
      password != null && !password.isEmpty();

    // Notify the result handler about the authentication outcome
    if (authenticationSuccess) {
      resultHandler.handle(Future.succeededFuture(true));
    } else {
      resultHandler.handle(Future.succeededFuture(false));
    }
  }


  public void registerUser(RoutingContext routingContext) {
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

      // Check if the username already exists in the database
      databaseService.doesUsernameExist(username, usernameExistenceResult -> {
        if (usernameExistenceResult.succeeded()) {
          boolean usernameExists = usernameExistenceResult.result();
          if (!usernameExists) {
            // Create a new User instance without setting the id manually
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            // Save the user to the database
            databaseService.saveUser(user, saveUserResult -> {
              if (saveUserResult.succeeded()) {
                logger.info("User saved successfully");
                // Respond with a success status code
                routingContext.response().setStatusCode(201).end("User registered successfully");
                logger.info("Registration successful");
              } else {
                logger.error("Failed to save user", saveUserResult.cause());
                // Respond with an error status code
                routingContext.fail(500);
              }
            });
          } else {
            // Respond with a message indicating that the username already exists
            routingContext.response().setStatusCode(409).end("Username already exists");
            logger.info("Registration failed - Username already exists");
          }
        } else {
          // Handle the result of the doesUsernameExist query failure
          routingContext.fail(500, usernameExistenceResult.cause());
          logger.error("Error checking username existence", usernameExistenceResult.cause());
        }
      });
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during user registration", e);
    }
  }

  public void getUsers(RoutingContext routingContext) {
    try {
      // Use the DatabaseService instance to retrieve users
      databaseService.getUsers(ar -> {
        if (ar.succeeded()) {
          List<User> users = ar.result();
          // Respond with the list of users as JSON
          routingContext.response().putHeader("Content-Type", "application/json").end(Json.encode(users));
          logger.info("Retrieved users successfully");
        } else {
          logger.error("Failed to retrieve users", ar.cause());
          // Respond with an error status code
          routingContext.fail(500);
        }
      });
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during getUsers", e);
    }
  }


}
