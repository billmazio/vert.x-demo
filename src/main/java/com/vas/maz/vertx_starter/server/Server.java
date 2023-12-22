package com.vas.maz.vertx_starter.server;

import com.vas.maz.vertx_starter.router.UserRouter;
import com.vas.maz.vertx_starter.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Server {

  private final Vertx vertx;
  private final UserService userService;

  public Server(Vertx vertx, UserService userService) {
    this.vertx = vertx;
    this.userService = userService;
  }

  public void start() {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("Content-Type"));

    router.route().handler(BodyHandler.create());

    UserRouter userRouter = new UserRouter(vertx, userService);
    router.mountSubRouter("/", userRouter.getRouter());

    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(false).setWebRoot("templates");
    router.route("/").handler(staticHandler);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080);

    System.out.println("Server started on port 8080");
  }
}
