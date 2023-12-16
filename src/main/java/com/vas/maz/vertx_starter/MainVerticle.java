package com.vas.maz.vertx_starter;

import com.vas.maz.vertx_starter.user.Server;
import com.vas.maz.vertx_starter.user.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());

  }

  @Override
  public void start() {
    UserService userService = new UserService(vertx);
    Server server = new Server(vertx, userService);
    server.start();
    System.out.println("MainAppVerticle started successfully");
  }

}
