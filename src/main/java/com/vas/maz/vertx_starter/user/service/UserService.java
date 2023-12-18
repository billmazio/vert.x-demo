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
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private DatabaseService databaseService;

  private final ThymeleafTemplateEngine templateEngine;

//  private static Vertx vertx;
//  private static final TemplateEngine templateEngine = ThymeleafTemplateEngine.create(vertx);


  public UserService(Vertx vertx) {
    // Create an instance of DatabaseService using vertx
    this.databaseService = new DatabaseService(JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:mysql://localhost:3306/vertex")
      .put("driver_class", "com.mysql.cj.jdbc.Driver")
      .put("user", "root")
      .put("password", "ww321278?")
    ));

    this.templateEngine = ThymeleafTemplateEngine.create(vertx);
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
                  .putHeader("Content-Type", "text/html")
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

                // Redirect to the login page after successful registration
                //  routingContext.response().putHeader("Location", "/").setStatusCode(303).end();
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

          // Create a Thymeleaf context
          Context thymeleafContext = new Context();
          thymeleafContext.setVariable("users", users);

          // Convert Thymeleaf context to Map
          Map<String, Object> thymeleafContextMap = new HashMap<>();
          // Convert Thymeleaf context to Map
          thymeleafContextMap = new HashMap<>();
          thymeleafContextMap.put("users", thymeleafContext.getVariable("users"));

          // Log the template path for debugging
          String templatePath = "template/users.html";

          // Render the Thymeleaf template
          templateEngine.render(thymeleafContextMap, templatePath, res -> {
            if (res.succeeded()) {
              String result = String.valueOf(res.result());
              if (result != null) {
                routingContext.response().putHeader("Content-Type", "text/html").end(result);
              } else {
                routingContext.fail(500);
              }
            } else {
              routingContext.fail(500, res.cause());
            }
          });

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

  public void updateUser(RoutingContext routingContext) {
    try {
      // Retrieve user information from the request, assuming you have appropriate form fields
      String idParam = routingContext.request().getParam("id");
      Long id = null;

      if (idParam != null) {
        try {
          id = Long.valueOf(idParam);
        } catch (NumberFormatException e) {
          // Handle the exception, e.g., log it or return an error response
          routingContext.fail(400); // Bad Request
          return;
        }
      }
      String username = routingContext.request().getParam("username");
      String newPassword = routingContext.request().getParam("password");

      // Create a User object with the updated information
      User updatedUser = new User(id, username, newPassword);

      // Use the DatabaseService instance to update the user
      databaseService.updateUser(updatedUser, ar -> {
        if (ar.succeeded()) {
          // User successfully updated

          // Redirect to the user list page or show a success message
          if (!routingContext.response().ended()) {
            routingContext.response().setStatusCode(303).putHeader("Location", "/updateUser").end();

          }

          logger.info("Updated user successfully");
        } else {
          // Failed to update user

          // Redirect to an error page or show an error message
          if (!routingContext.response().ended()) {
            routingContext.fail(500);
          }

          logger.error("Failed to update user", ar.cause());
        }
      });

      // Retrieve and render users after the update (similar to getUsers logic)
      databaseService.getUsers(usersResult -> {
        if (usersResult.succeeded()) {
          List<User> users = usersResult.result();

          // Add the updated user to the Thymeleaf context
          Context thymeleafContext = new Context();
          thymeleafContext.setVariable("user", updatedUser);
          thymeleafContext.setVariable("users", users);

          // Convert Thymeleaf context to Map
          Map<String, Object> thymeleafContextMap = new HashMap<>();
          thymeleafContextMap.put("user", thymeleafContext.getVariable("user"));
          thymeleafContextMap.put("users", thymeleafContext.getVariable("users"));

          // Log the template path for debugging
          String templatePath = "template/updateUser.html";

          // Render the Thymeleaf template
          templateEngine.render(thymeleafContextMap, templatePath, res -> {
            if (res.succeeded()) {
              String result = String.valueOf(res.result());
              if (result != null && !routingContext.response().ended()) {
                routingContext.response().putHeader("Content-Type", "text/html").end(result);
              } else {
                routingContext.fail(500);
              }
            } else {
              routingContext.fail(500, res.cause());
            }
          });

          logger.info("Retrieved users successfully after update");
        } else {
          logger.error("Failed to retrieve users after update", usersResult.cause());
          // Respond with an error status code
          routingContext.fail(500);
        }
      });
    } catch (NumberFormatException e) {
      // Handle the case where userId couldn't be parsed to Long
      routingContext.fail(400, e);
      logger.error("Invalid userId format", e);
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during updateUser", e);
    }
  }





}








