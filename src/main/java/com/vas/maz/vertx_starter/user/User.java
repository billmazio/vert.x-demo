package com.vas.maz.vertx_starter.user;

import io.vertx.core.json.JsonObject;

public class User {
  private Long id; // Assuming Long for flexibility, adjust as needed
  private String username;
  private String password;


  public User() {
  }

  public User(Long id, String username, String password) {
    this.username = username;
    this.password = password;
    this.id = id;
  }



  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}