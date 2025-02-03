package com.cookbook;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class LoginController {
  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private AnchorPane root;
  @FXML
  private ImageView backgroundImageView;

  private Main main;
  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  public void setMain(Main main) {
    this.main = main;
  }

  public void initialize() {
    passwordField.setOnAction(e -> login());
    playBackgroundAnimation();
  }

  public void login() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    int userId = getUserId(username, password);

    if (userId > 0) { // A valid user ID was found
      if (username.equals("admin")) {
        main.switchToAdminPage(); // Admin login
      } else {
        main.switchToHome(username, userId); // Pass the user ID when switching to home page
      }
    } else {
      showAlert("Login failed. User with these credentials doesn't exist. Check your password and username!", AlertType.ERROR);
    }
  }

  private boolean authenticate(String username, String password) {
    String url = "jdbc:mysql://localhost:3306/Projectcourse";
    String user = "root";
    String pass = "minecraft.co.id";

    try (Connection conn = DriverManager.getConnection(url, user, pass)) {
      String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      statement.setString(2, hashPassword(password));
      ResultSet resultSet = statement.executeQuery();

      return resultSet.next();
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

  public void switchToRegistration() throws IOException {
    main.switchToRegistration();
  }

  private int getUserId(String username, String password) {
    int userId = -1; // Default to invalid ID

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      String sql = "SELECT UserID FROM Users WHERE Username = ? AND Password = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, username);
      statement.setString(2, hashPassword(password));
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        userId = resultSet.getInt("UserID");
      }

      resultSet.close(); // Close resources
      statement.close();
    } catch (SQLException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return userId; // Return the found UserID or -1 if not found
  }

    private void playBackgroundAnimation() {
    // Ensure the root is initialized
    if (root != null) {
        // Set the initial image
        Image initialImage = new Image("file:/Users/keenansylo/Documents/1DV508 - Project Course in Computer/working cookbook/cookbook/cookbook/src/main/resources/com/cookbook/image/xwing.png");
        backgroundImageView.setImage(initialImage);

        // Set initial position of the ImageView outside the left boundary
        backgroundImageView.setTranslateX(-backgroundImageView.getFitWidth());

        // Create a TranslateTransition to move the image from left to right, out of the screen
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(2), backgroundImageView);
        translateTransition.setFromX(-backgroundImageView.getFitWidth());
        translateTransition.setToX(root.getWidth() + backgroundImageView.getFitWidth());
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);

        // Create a PauseTransition for cooldown
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(4));

        // SequentialTransition to combine the translate and pause transitions
        SequentialTransition sequentialTransition = new SequentialTransition(translateTransition, pauseTransition);

        // Set an action to repeat the sequential transition when it finishes
        sequentialTransition.setOnFinished(event -> {
            backgroundImageView.setTranslateX(-backgroundImageView.getFitWidth()); // Reset position to the left side
            sequentialTransition.play(); // Start the animation again
        });

        // Start the animation
        sequentialTransition.play();
    } else {
      System.err.println("Root is not initialized.");
    }
  }
}
