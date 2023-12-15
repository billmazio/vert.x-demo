package com.vas.maz.vertx_starter.user.service;

import com.vas.maz.vertx_starter.user.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.ArrayList;
import java.util.List;

import static io.vertx.sqlclient.impl.SocketConnectionBase.logger;

public class DatabaseService {

  private JDBCClient jdbcClient;

  public DatabaseService(JDBCClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public DatabaseService() {

  }

  public void saveUser(User user, Handler<AsyncResult<Void>> resultHandler) {
    // Check if a user with the same username already exists
    String checkUserQuery = "SELECT id FROM users WHERE username = ?";
    jdbcClient.querySingleWithParams(checkUserQuery, new JsonArray().add(user.getUsername()), checkUser -> {
      if (checkUser.failed()) {
        // Handle the query failure
        resultHandler.handle(Future.failedFuture(checkUser.cause()));
      } else {
        if (checkUser.result() != null) {
          // User with the same username already exists, handle accordingly
          resultHandler.handle(Future.failedFuture("Username already exists"));
        } else {
          // Insert the new user into the database
          String insertUserQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
          JsonArray params = new JsonArray().add(user.getUsername()).add(user.getPassword());
          jdbcClient.updateWithParams(insertUserQuery, params, insertResult -> {
            if (insertResult.failed()) {
              // Handle the insert failure
              resultHandler.handle(Future.failedFuture(insertResult.cause()));
            } else {
              // User successfully inserted
              resultHandler.handle(Future.succeededFuture());
            }
          });
        }
      }
    });
  }

  public void doesUsernameExist(String username, Handler<AsyncResult<Boolean>> resultHandler) {
    jdbcClient.getConnection(res -> {
      if (res.succeeded()) {
        SQLConnection connection = res.result();
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        connection.queryWithParams(query, new JsonArray().add(username), ar -> {
          if (ar.succeeded()) {
            ResultSet resultSet = ar.result();
            // Assuming the first row of the result set contains the count
            int count = resultSet.getResults().get(0).getInteger(0);
            resultHandler.handle(Future.succeededFuture(count > 0));
          } else {
            logger.error("Error checking username existence", ar.cause());
            resultHandler.handle(Future.failedFuture(ar.cause()));
          }
          connection.close();
        });
      } else {
        logger.error("Error getting database connection", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }



public void getUsers(Handler<AsyncResult<List<User>>> resultHandler) {
  jdbcClient.getConnection(conn -> {
    if (conn.succeeded()) {
      SQLConnection connection = conn.result();
      try {
        connection.query("SELECT * FROM users", query -> {
          if (query.succeeded()) {
            ResultSet resultSet = query.result();
            List<User> users = new ArrayList<>();
            for (JsonObject row : resultSet.getRows()) {
              users.add(new User(row.getLong("id"), row.getString("username"), row.getString("password")));
            }
            resultHandler.handle(Future.succeededFuture(users));
          } else {
            resultHandler.handle(Future.failedFuture(query.cause()));
          }
        });
      } finally {
        connection.close();
      }
    } else {
      resultHandler.handle(Future.failedFuture(conn.cause()));
    }
  });
}
  // Other database operations...
}

