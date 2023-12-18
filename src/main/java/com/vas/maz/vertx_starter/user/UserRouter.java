package com.vas.maz.vertx_starter.user;

import com.vas.maz.vertx_starter.user.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;


public class UserRouter {

  private final Router router;

  public UserRouter(Vertx vertx, UserService userService) {
    this.router = Router.router(vertx);



    router.post("/register").handler(userService::registerUser);
    router.post("/login").handler(userService::login);
    router.get("/users").handler(userService::getUsers);

    // Update the route to use PUT
    router.route("/updateUser").handler(userService::updateUser);


    // router.post("/updateUser").handler(userService::updateUser);




    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("static");
    router.route("/static/*").handler(staticHandler);



  }



  public Router getRouter() {
    return router;
  }
}



