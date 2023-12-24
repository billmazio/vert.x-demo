package com.vas.maz.vertx_starter.router;

import com.vas.maz.vertx_starter.service.DatabaseService;
import com.vas.maz.vertx_starter.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class UserRouter {

  private  DatabaseService databaseService;
  private Router router;

  public UserRouter(Vertx vertx, UserService userService) {
    this.router = Router.router(vertx);


    router.post("/register").handler(userService::registerUser);
    router.post("/login").handler(userService::login);
    router.get("/users").handler(userService::getUsers);
    router.post("/editUser").handler(userService::updateUser);
    router.post("/deleteUser").handler(userService::deleteUser);
    router.post("/editUser/:id").handler(routingContext -> {
        userService.updateUser(routingContext); // Handle user update

        // Redirect to the users page after successful update using JavaScript
        String redirectScript = "<script>window.location.href = '/users';</script>";
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .end(redirectScript);
      });








    // Static handler for serving static resources
    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("static");
    router.route("/static/*").handler(staticHandler);


  }


  public Router getRouter() {
    return router;
  }


}


