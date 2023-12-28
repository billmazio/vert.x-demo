package com.vas.maz.vertx_starter.service;

import com.vas.maz.vertx_starter.model.User;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vertx.sqlclient.impl.SocketConnectionBase.logger;


public class UserService {


  private DatabaseService databaseService;

  private final ThymeleafTemplateEngine templateEngine;
  private static final String errorMessage = "Username already exists";


  public UserService(Vertx vertx) {
    // Create an instance of DatabaseService using vertx
    this.databaseService = new DatabaseService(JDBCClient.createShared(vertx, new JsonObject()

      .put("url", "jdbc:mysql://localhost:3306/vertex")
      .put("driver_class", "com.mysql.cj.jdbc.Driver")
      .put("user", "root")
      .put("password", "ww321278?")
      .put("max_pool_size", 30) // Adjust these values based on your requirements
      .put("initial_pool_size", 5)
      .put("min_pool_size", 3)
      .put("max_idle_time", 60000)
    ));

    this.templateEngine = ThymeleafTemplateEngine.create(vertx);
  }

  public void login(RoutingContext routingContext) {
    try {
      JsonObject requestBody = routingContext.getBodyAsJson();
      if (requestBody == null || requestBody.isEmpty()) {
        routingContext.fail(400); // Bad Request
        return;
      }

      String username = requestBody.getString("username");
      String password = requestBody.getString("password");

      // Use the DatabaseService instance to authenticate the user
      databaseService.getUserPassword(username, password, authResult -> {
        if (authResult.succeeded() && authResult.result()) {
          // Authentication succeeded
          routingContext.response()
            .putHeader("Content-Type", "text/html")
            .putHeader("Location", "/")
            .setStatusCode(303) // See Other
            .end();
          logger.info("Login successful");
        } else {
          // Authentication failed
          routingContext.response().setStatusCode(401).end(Json.encode(new JsonObject().put("success", false).put("message", "Authentication failed")));
          logger.info("Login failed - Authentication failed");
        }
      });
    } catch (Exception e) {
      routingContext.fail(500, e);
      logger.error("Error during login", e);
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
           routingContext.response().setStatusCode(409).end(errorMessage);

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
          String templatePath = "templates/users.html";

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

  public Future<Void> updateUser(RoutingContext routingContext) {
    Promise<Void> promise = Promise.promise();

    try {
      Long id = parseIdParam(routingContext);

      String username = routingContext.request().getParam("username");
      String newPassword = routingContext.request().getParam("password");

      // Check if the updated username already exists
      databaseService.doesUsernameExist(username, usernameExistenceResult -> {
        if (usernameExistenceResult.succeeded()) {
          boolean usernameExists = usernameExistenceResult.result();
          if (!usernameExists) {
            // Render the Thymeleaf template before proceeding with the update
            renderThymeleafTemplate(routingContext, new User(id, username, newPassword));

            // Username doesn't exist, proceed with the update
            User updatedUser = new User(id, username, newPassword);

            databaseService.updateUser(updatedUser, updateResult -> {
              if (updateResult.succeeded()) {
                routingContext.put("updateSuccess", true);
                logger.info("User updated successfully");

                // Reroute to "/users" after a successful update


                promise.complete();
              //  routingContext.reroute("/users");
              } else {
                routingContext.response().setStatusCode(500).end("Failed to update user");
                handleUpdateFailure(routingContext, updateResult.cause());
                promise.fail(updateResult.cause());

              }
            });
          } else {
            // Respond with a message indicating that the username already exists
            routingContext.response().setStatusCode(409).end("Username already exists");

            logger.info("Update failed - Username already exists");
            promise.fail("Username already exists");
          }
        } else {
          // Handle the result of the doesUsernameExist query failure
          routingContext.response().setStatusCode(500).end("Failed to check username existence");
          routingContext.fail(500, usernameExistenceResult.cause());
          logger.error("Error checking username existence", usernameExistenceResult.cause());
          promise.fail(usernameExistenceResult.cause());
        }
      });
    } catch (Exception e) {
    //  routingContext.response().setStatusCode(500).end("Error during updateUser");
      routingContext.response().ended();
      routingContext.fail(500, e);
      logger.error("Error during updateUser", e);
      promise.fail(e);
    }

    return promise.future();
  }

  // Helper method to render Thymeleaf template
  private void renderThymeleafTemplate(RoutingContext routingContext, User user) {
    // Add the updated user to the Thymeleaf context
    Context thymeleafContext = new Context();
    thymeleafContext.setVariable("user", user);

    // Convert Thymeleaf context to Map
    Map<String, Object> thymeleafContextMap = new HashMap<>();
    thymeleafContextMap.put("user", thymeleafContext.getVariable("user"));

    // Log the template path for debugging
    String templatePath = "templates/editUser.html";

    // Render the Thymeleaf template
    templateEngine.render(thymeleafContextMap, templatePath, res -> {
      if (res.succeeded()) {
        String result = String.valueOf(res.result());
        if (result != null && !routingContext.response().ended()) {
          routingContext.response().putHeader("Content-Type", "text/html").end(result);

          // Log the update success now that rendering is successful
          logger.info("User successfully scheduled for update. Redirecting to editUser page.");
        } else {
          routingContext.fail(500);
        }
      } else {
        // Log the failure to render the Thymeleaf template
        routingContext.fail(500, res.cause());
      }
    });
  }

  private Long parseIdParam(RoutingContext routingContext) {
    String idParam = routingContext.request().getParam("id");

    if (idParam != null && !idParam.isEmpty()) {
      try {
        return Long.parseLong(idParam);
      } catch (NumberFormatException e) {
        routingContext.fail(400);
        throw new RuntimeException("Invalid userId format", e);
      }
    }

    return null;
  }

  // Helper method to handle update failures
  private void handleUpdateFailure(RoutingContext routingContext, Throwable cause) {
    logger.error("Failed to update user", cause);

    // Respond with an error status code and message
    routingContext.response().setStatusCode(500).end("Failed to update user");
  }




  public void deleteUser(RoutingContext routingContext) {
    try {
      // Retrieve user ID from the request, assuming you have appropriate form fields
      String idParam = routingContext.request().getParam("id");
      Long id = null;

      if (idParam != null && !idParam.isEmpty()) {
        try {
          id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
          // Handle the exception, e.g., log it or return an error response
          routingContext.fail(400); // Bad Request
          return;
        }
      } else {
        // Handle the case where user ID is not provided
        routingContext.fail(400); // Bad Request
        return;
      }

      // Create a User object with the ID
      User userToDelete = new User();
      userToDelete.setId(id);

      // Use the DatabaseService instance to delete the user
      databaseService.deleteUser(userToDelete, deleteResult -> {
        if (deleteResult.succeeded()) {
          // User successfully deleted

          // Set a flag indicating the deletion success in the routing context
          routingContext.put("deleteSuccess", true);

          // Retrieve and render users after the deletion
          retrieveAndRenderUsers(routingContext);
        } else {
          // Failed to delete user

          // Log the error
          logger.error("Failed to delete user", deleteResult.cause());

          // Redirect to an error page or show an error message
          routingContext.response().setStatusCode(500).end("Failed to delete user");
        }
      });
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during deleteUser", e);
    }
  }


  // Helper method to retrieve and render users
  private void retrieveAndRenderUsers(RoutingContext routingContext) {
    databaseService.getUsers(usersResult -> {
      if (usersResult.succeeded()) {
        // Add the user list to the Thymeleaf context
        List<User> users = usersResult.result();
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("users", users);

        // Convert Thymeleaf context to Map
        Map<String, Object> thymeleafContextMap = new HashMap<>();
        thymeleafContextMap.put("users", thymeleafContext.getVariable("users"));

        // Log the template path for debugging
        String templatePath = "templates/users.html";

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

        logger.info("Retrieved users successfully after deletion");
      } else {
        logger.error("Failed to retrieve users after deletion", usersResult.cause());
        // Respond with an error status code
        routingContext.fail(500);
      }
    });
  }



}



