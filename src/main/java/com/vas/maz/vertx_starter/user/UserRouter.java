package com.vas.maz.vertx_starter.user;
import com.vas.maz.vertx_starter.user.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.StaticHandler;


public class UserRouter {

  private final Router router;

  public UserRouter(Vertx vertx, UserService userService) {
    this.router = Router.router(vertx);

    router.post("/api/register").handler(userService::registerUser);
    router.post("/login").handler(userService::login);
    router.route("/users").handler(userService::getUsers);


    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("static");
    router.route("/static/*").handler(staticHandler);
  }

  public Router getRouter() {
    return router;
  }
}



