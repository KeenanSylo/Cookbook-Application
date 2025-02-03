// Main.java
package com.cookbook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  private Stage primaryStage;

  @Override
  public void start(Stage primaryStage) throws IOException {
    this.primaryStage = primaryStage;

    // Create a new StackPane for the animation
    StackPane root = new StackPane();
    root.setStyle("-fx-background-color: #faf0e6;"); // Set the background color

    // Play the startup animation
    AnimationUtil.playStartupAnimation(root, primaryStage, this::switchToLogin);

    // Set the scene to the primaryStage
    primaryStage.setScene(new Scene(root, 539, 439)); // Set the same size as your login and register scenes
    primaryStage.show();
  }

  public void switchToLogin() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
      Parent root = loader.load();
      LoginController loginController = loader.getController();
      loginController.setMain(this);
      Scene loginScene = new Scene(root);
      primaryStage.setTitle("Login");
      primaryStage.setScene(loginScene);
      primaryStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void switchToRegistration() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("registration.fxml"));
      Parent root = loader.load();
      RegistrationController registrationController = loader.getController();
      registrationController.setMain(this);
      Scene registrationScene = new Scene(root);
      primaryStage.setTitle("Registration");
      primaryStage.setScene(registrationScene);
      primaryStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void switchToAdminPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("admin.fxml"));
      Parent root = loader.load();
      AdminController adminController = loader.getController();
      adminController.setStage(primaryStage);
      adminController.setMain(this); // Pass the instance of Main to the controller
      Scene adminScene = new Scene(root);
      primaryStage.setTitle("Admin Page");
      primaryStage.setScene(adminScene);
      primaryStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Method to open the "Add Recipe" window
  public void openAddRecipeWindow() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("addrecipe.fxml"));
      Parent root = loader.load();

      // Get the controller from the FXMLLoader
      AddRecipeController addRecipeController = loader.getController();

      // Initialize a new Stage for this window
      Stage recipeStage = new Stage();
      recipeStage.setTitle("Add Recipe");

      // Set the scene for the new stage
      Scene recipeScene = new Scene(root);
      recipeStage.setScene(recipeScene);

      // Pass references to the controller
      addRecipeController.setStage(recipeStage);
      addRecipeController.setMain(this); // Pass 'Main' instance to the controller

      // Show the new stage
      recipeStage.show();
    } catch (IOException e) {
      e.printStackTrace(); // Improved error handling, you could add logging here
    }
  }

  public void openViewDinnerWindow() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("weeklyDinner.fxml"));
      Parent root = loader.load();

      // Get the controller from the FXMLLoader
      WeeklyDinnerController dinnerController = loader.getController();

      // Initialize a new Stage for this window
      Stage dinnerStage = new Stage();
      dinnerStage.setTitle("View Dinner");

      // Set the scene for the new stage
      Scene dinnerScene = new Scene(root);
      dinnerStage.setScene(dinnerScene);

      // Pass references to the controller
      dinnerController.setStage(dinnerStage);
      dinnerController.setMain(this);

      // Show the new stage
      dinnerStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void switchToHome(String username, int userId) {
    try {
      Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

      // Prepare statement to fetch DisplayName and UserID
      String query = "SELECT DisplayName FROM Users WHERE Username = ?";
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setString(1, username);

      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        String displayName = rs.getString("DisplayName");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("test.fxml"));
        Parent root = loader.load();

        HomeController homeController = loader.getController();
        homeController.setWelcomeMessage(displayName); // Set display name
        homeController.setMain(this); // Set main instance
        homeController.setCurrentUserId(userId); // Pass user ID

        Scene homeScene = new Scene(root);
        primaryStage.setTitle("Home");
        primaryStage.setScene(homeScene);
        primaryStage.show();
      } else {
        System.out.println("User not found.");
      }

      rs.close(); // Close resources
      statement.close();
      conn.close();
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
