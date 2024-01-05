package com.vas.maz.vertx_starter.router;

import com.vas.maz.vertx_starter.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class UserRouter {

  private final Router router;
  private final UserService userService;

  public UserRouter(Vertx vertx, UserService userService) {
    this.router = Router.router(vertx);
    this.userService = userService;

    // Setup routes
    setupRoutes();
  }

  private void setupRoutes() {
    // Static handler for serving static resources
    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("static");
    router.route("/static/*").handler(staticHandler);

    // Public routes
    router.post("/register").handler(userService::registerUser);
    router.post("/login").handler(userService::login);

    // Protect routes under /users/* using JWT authentication
    router.route("/users/*").handler(JWTAuthHandler.create(userService.getJwtAuth()));

    // Secured routes
    router.get("/users").handler(userService::getUsers);
    router.post("/editUser").handler(userService::updateUser);
    router.post("/editUser/:id").handler(userService::updateUser);
    router.post("/deleteUser").handler(userService::deleteUser);
  }

  public Router getRouter() {
    return router;
  }
}
