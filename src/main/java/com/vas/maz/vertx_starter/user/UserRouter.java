package com.vas.maz.vertx_starter.user;

import com.vas.maz.vertx_starter.user.service.DatabaseService;
import com.vas.maz.vertx_starter.user.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class UserRouter {

  private DatabaseService databaseService;
  private Router router;

  public UserRouter(Vertx vertx, UserService userService) {
    this.router = Router.router(vertx);


    router.post("/register").handler(userService::registerUser);
    router.post("/login").handler(userService::login);
    router.get("/users").handler(userService::getUsers);
    router.post("/editUser").handler(userService::updateUser);

    router.post("/deleteUser").handler(userService::deleteUser);

// Your route handler for the deleteUser endpoint
//    router.post("/deleteUser").handler(routingContext -> {
//      String method = routingContext.request().getParam("_method");
//      if ("delete".equalsIgnoreCase(method)) {
//        // Handle the delete operation
//        String userId = routingContext.request().getParam("id");
//        // Convert userId to Long and perform the delete operation
//        // ...
//
//        // Redirect or respond as needed
//        routingContext.response().setStatusCode(303).putHeader("Location", "/users").end();
//      } else {
//        // Handle other POST requests
//        // ...
//      }
//    });

    // Static handler for serving static resources
    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("static");
    router.route("/static/*").handler(staticHandler);



  }



  public Router getRouter () {
    return router;
  }
}


