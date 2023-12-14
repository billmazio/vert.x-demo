package com.vas.maz.vertx_starter.user;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.sql.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
public class DatabaseService {

  private final JDBCClient jdbcClient;

  public DatabaseService(JDBCClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void saveUser(User user, Handler<AsyncResult<Void>> resultHandler) {
    String sql = "INSERT INTO user (id, username, password) VALUES (?, ?, ?)";
    JsonArray params = new JsonArray().add(user.getId()).add(user.getUsername()).add(user.getPassword());

    jdbcClient.updateWithParams(sql, params, ar -> {
      if (ar.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  // Other database operations...
}

