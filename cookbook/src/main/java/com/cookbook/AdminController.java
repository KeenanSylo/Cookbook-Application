package com.cookbook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminController {
  @FXML
  private TextField usernameField;
  @FXML
  private TextField displayNameField;
  @FXML
  private PasswordField passwordField;

  private Stage stage;
  private Main main;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setMain(Main main) {
    this.main = main;
  }

  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  public void addUser() {
    String username = usernameField.getText();
    String displayName = displayNameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
      showAlert("All fields are required.", AlertType.ERROR);
      return;
    }

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String checkSql = "SELECT * FROM Users WHERE username = ?";
      PreparedStatement checkStatement = conn.prepareStatement(checkSql);
      checkStatement.setString(1, username);
      if (checkStatement.executeQuery().next()) {
        showAlert("A user with this username already exists!", AlertType.ERROR);
        return;
      }

      String sql = "INSERT INTO Users (username, password, DisplayName) VALUES (?, ?, ?)";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      statement.setString(2, hashPassword(password));
      statement.setString(3, displayName);
      int rowsInserted = statement.executeUpdate();

      if (rowsInserted > 0) {
        showAlert("User added successfully!", AlertType.INFORMATION);
      } else {
        showAlert("Failed to add user. Please try again.", AlertType.ERROR);
      }
    } catch (SQLException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      showAlert("An error occurred. Please try again.", AlertType.ERROR);
    }
  }

  public void deleteUser() {
    String username = usernameField.getText();

    if (username.isEmpty()) {
      showAlert("Please enter a username.", AlertType.ERROR);
      return;
    }

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String sql = "DELETE FROM Users WHERE username = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      int rowsDeleted = statement.executeUpdate();

      if (rowsDeleted > 0) {
        showAlert("User deleted successfully!", AlertType.INFORMATION);
      } else {
        showAlert("User not found.", AlertType.ERROR);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      showAlert("An error occurred. Please try again.", AlertType.ERROR);
    }
  }

  public void editUser() {
    String username = usernameField.getText();
    String displayName = displayNameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
      showAlert("All fields are required.", AlertType.ERROR);
      return;
    }

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String sql = "UPDATE Users SET password = ?, DisplayName = ? WHERE username = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, hashPassword(password));
      statement.setString(2, displayName);
      statement.setString(3, username);
      int rowsUpdated = statement.executeUpdate();

      if (rowsUpdated > 0) {
        showAlert("User updated successfully!", AlertType.INFORMATION);
      } else {
        showAlert("User not found.", AlertType.ERROR);
      }
    } catch (SQLException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      showAlert("An error occurred. Please try again.", AlertType.ERROR);
    }
  }

  public void logout() {
    // Close the current admin page and switch back to the login page
    stage.close();
    main.switchToLogin();
  }

  private String hashPassword(String password) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(password.getBytes());
    byte[] digest = md.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private void showAlert(String message, AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
