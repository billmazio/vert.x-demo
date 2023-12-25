package com.vas.maz.vertx_starter.service;

import com.vas.maz.vertx_starter.model.User;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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



  //  public void updateUser(RoutingContext routingContext) {
//    try {
//      // Retrieve user information from the request, assuming you have appropriate form fields
//      String idParam = routingContext.request().getParam("id");
//      Long id = null;
//
//      if (idParam != null && !idParam.isEmpty()) {
//        try {
//          id = Long.parseLong(idParam);
//        } catch (NumberFormatException e) {
//          // Handle the exception, e.g., log it or return an error response
//          routingContext.fail(400); // Bad Request
//          return;
//        }
//      }
//      String username = routingContext.request().getParam("username");
//      String newPassword = routingContext.request().getParam("password");
//
//      // Create a User object with the updated information
//      User updatedUser = new User(id, username, newPassword);
//
//      // Use the DatabaseService instance to update the user
//      databaseService.updateUser(updatedUser, ar -> {
//        if (ar.succeeded()) {
//          // User successfully updated
//
//          // Set a flag indicating the update success in the routing context
//          routingContext.put("updateSuccess", true);
//
//          // Redirect to the user list page
//          //  routingContext.response().setStatusCode(303).putHeader("Location", "/users").end();
//        } else {
//          // Failed to update user
//
//          // Log the error
//          logger.error("Failed to update user", ar.cause());
//
//          // Redirect to an error page or show an error message
//          routingContext.response().setStatusCode(500).end("Failed to update user");
//        }
//      });
//
//      // Retrieve and render users after the update (similar to getUsers logic)
//      databaseService.getUsers(usersResult -> {
//        if (usersResult.succeeded()) {
//
//          // Add the updated user to the Thymeleaf context
//          Context thymeleafContext = new Context();
//          thymeleafContext.setVariable("user", updatedUser);
//
//
//          // Convert Thymeleaf context to Map
//          Map<String, Object> thymeleafContextMap = new HashMap<>();
//          thymeleafContextMap.put("user", thymeleafContext.getVariable("user"));
//
//          // Log the template path for debugging
//          String templatePath = "templates/editUser.html";
//
//          // Render the Thymeleaf template
//          // Disable Thymeleaf caching
//
//
//          templateEngine.render(thymeleafContextMap, templatePath, res -> {
//            if (res.succeeded()) {
//              String result = String.valueOf(res.result());
//              if (result != null && !routingContext.response().ended()) {
//                routingContext.response().putHeader("Content-Type", "text/html").end(result);
//
//              } else {
//                routingContext.fail(500);
//              }
//              // Log the update success here
//
//            } else {routingContext.fail(500, res.cause());
//            }
//          });
//
//          logger.info("goes to update-form");
//        } else {
//          logger.error("Failed to retrieve users after update", usersResult.cause());
//          // Respond with an error status code
//          routingContext.fail(500);
//        }
//      });
//    } catch (NumberFormatException e) {
//      // Handle the case where userId couldn't be parsed to Long
//      routingContext.fail(400, e);
//      logger.error("Invalid userId format", e);
//    } catch (Exception e) {
//      // Handle any unexpected exceptions
//      routingContext.fail(500, e);
//      logger.error("Error during updateUser", e);
//    }
//
//  }
  public Future<Void> updateUser(RoutingContext routingContext) {
    Promise<Void> promise = Promise.promise();
    try {
      // Retrieve user information from the request, assuming you have appropriate form fields
      String idParam = routingContext.request().getParam("id");
      Long id = null;

      if (idParam != null && !idParam.isEmpty()) {
        try {
          id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
          // Handle the exception, e.g., log it or return an error response
          routingContext.fail(400); // Bad Request
          return null;
        }
      }
      String username = routingContext.request().getParam("username");
      String newPassword = routingContext.request().getParam("password");

      // Create a User object with the updated information
      User updatedUser = new User(id, username, newPassword);

      // Use the DatabaseService instance to update the user
      databaseService.updateUser(updatedUser, updateResult -> {
        if (updateResult.succeeded()) {
          // User successfully updated

          routingContext.put("updateSuccess", true);
          logger.info("User successfully updated. updateSuccess set to true.");


          // Log the update success only if rendering is successful
          renderThymeleafTemplate(routingContext, updatedUser);

          promise.complete(); // Complete the promise when the operation is successful
        } else {
          // Failed to update user
          handleUpdateFailure(routingContext, updateResult.cause());

          promise.fail(updateResult.cause()); // Fail the promise with the cause of the failure
        }
      });

    } catch (NumberFormatException e) {
      // Handle the case where userId couldn't be parsed to Long
      routingContext.fail(400, e);
      logger.error("Invalid userId format", e);
      promise.fail(e); // Fail the promise with the exception
    } catch (Exception e) {
      // Handle any unexpected exceptions
      routingContext.fail(500, e);
      logger.error("Error during updateUser", e);
      promise.fail(e); // Fail the promise with the exception
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
          logger.info("User updated successfully, goes to update-form");
        } else {
          routingContext.fail(500);
        }
      } else {
        // Log the failure to render the Thymeleaf template
        routingContext.fail(500, res.cause());
      }
    });
  }

  // Helper method to handle update failures
  private void handleUpdateFailure(RoutingContext routingContext, Throwable cause) {
    // Log the error
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



