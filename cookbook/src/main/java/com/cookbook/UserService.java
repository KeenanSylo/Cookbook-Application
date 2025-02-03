package com.cookbook;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  public boolean authenticateUser(String username, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String sql = "SELECT password FROM Users WHERE username = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        String hashedPassword = resultSet.getString("password");
        return BCrypt.checkpw(password, hashedPassword);
      } else {
        return false; // User not found
      }
    }
  }

  public boolean createUser(String username, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      if (userExists(conn, username)) {
        return false; // User already exists
      } else {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, username);
        statement.setString(2, hashedPassword);
        int rowsInserted = statement.executeUpdate();
        return rowsInserted > 0;
      }
    }
  }

  private boolean userExists(Connection conn, String username) throws SQLException {
    String sql = "SELECT * FROM Users WHERE username = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, username);
    ResultSet resultSet = statement.executeQuery();
    return resultSet.next();
  }
}
