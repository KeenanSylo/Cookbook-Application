package com.cookbook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

public class RegistrationController {
  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private TextField displaynameField;
  @FXML
  private AnchorPane root;

  private Main main;
  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  public void setMain(Main main) {
    this.main = main;
  }

  public void initialize() {
    passwordField.setOnAction(e -> register());
  }

  public void register() {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String displayName = displaynameField.getText(); // Retrieve display name

    if (username.isEmpty() || password.isEmpty()) {
      showAlert("Username and password are required.", AlertType.ERROR);
      return;
    }

    if (registerUser(username, password, displayName)) { // Pass display name to registerUser
      showAlert("User registered successfully!", AlertType.INFORMATION);
    } else {
      showAlert("Registration failed. Please try again.", AlertType.ERROR);
    }
  }

  private boolean registerUser(String username, String password, String displayName) {

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String checkSql = "SELECT * FROM Users WHERE username = ?";
      PreparedStatement checkStatement = conn.prepareStatement(checkSql);
      checkStatement.setString(1, username);
      if (checkStatement.executeQuery().next()) {
        showAlert("A user with this username already exists!", AlertType.ERROR);
        return false;
      }

      String sql = "INSERT INTO Users (username, password, DisplayName) VALUES (?, ?, ?)";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      statement.setString(2, hashPassword(password));
      statement.setString(3, displayName); // Add display name to the statement
      int rowsInserted = statement.executeUpdate();

      return rowsInserted > 0;
    } catch (SQLException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      return false;
    }
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

  public void switchToLogin() throws IOException {
    main.switchToLogin();
  }

}
