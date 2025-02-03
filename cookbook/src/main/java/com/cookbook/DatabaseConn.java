package com.cookbook;

public class DatabaseConn {
  // Declare the variables
  private String dbUrlString;
  private String dbUserString;
  private String dbPasswordString;

  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  // Corrected constructor
  public DatabaseConn(String dbUrlString, String dbUserString, String dbPasswordString) {
    // Correctly assign parameter values to instance variables
    this.dbUrlString = dbUrlString;
    this.dbUserString = dbUserString;
    this.dbPasswordString = dbPasswordString;
  }

  // Getter for URL
  public String getUrl() {
    return dbUrlString;
  }

  // Getter for user
  public String getUser() {
    return dbUserString;
  }

  // Getter for password
  public String getPassword() {
    return dbPasswordString;
  }
}
