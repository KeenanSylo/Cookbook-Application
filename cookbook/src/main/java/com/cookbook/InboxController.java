package com.cookbook;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InboxController {

    @FXML
    private FlowPane inbox;

    @FXML
    private ScrollPane inboxed;

    @FXML
    private Button home;

    private VBox contentBox;

    private HomeController homeController;

    private Label selectedLabel;

    @FXML
    public void initialize() {
        // Initialize contentBox and add it to the ScrollPane
        contentBox = new VBox();
        inboxed.setContent(contentBox);
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
        // Load initial content now that homeController is set
        loadInboxContent();
    }

    private void loadInboxContent() {
        if (homeController == null) {
            System.err.println("HomeController is not set");
            return;
        }

        int userId = homeController.getCurrentUserId();
        String url = "jdbc:mysql://localhost:3306/Projectcourse"; // Removed space in the database name
        String user = "root";
        String pass = "minecraft.co.id";

        String query = "SELECT r.RecipeID, r.Name AS recipe_name, u.Username AS sender_name, sr.date_time, sr.message " +
                "FROM SentRecipes sr " +
                "JOIN Recipes r ON sr.recipe_id = r.RecipeID " +
                "JOIN Users u ON sr.sender_id = u.UserID " +
                "WHERE sr.receiver_id = ? " +
                "ORDER BY sr.date_time DESC";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int recipeId = resultSet.getInt("RecipeID");
                String recipeName = resultSet.getString("recipe_name");
                String senderName = resultSet.getString("sender_name");
                String dateTime = resultSet.getString("date_time");
                String message = resultSet.getString("message");

                String displayText = String.format("%s was sent by %s at %s. \n(%s)",
                        recipeName, senderName, dateTime, message);
                Label label = new Label(displayText);
                label.setUserData(recipeId); // Store recipeId in the label

                // Set the style for bold and larger text
                label.setStyle("-fx-font-size: 16px; -fx-border-color: gray; -fx-padding: 10;");

                // Add a mouse click event to select the label and show recipe overlay
                label.setOnMouseClicked(this::handleLabelClick);

                contentBox.getChildren().add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors here
        }
    }

    private void handleLabelClick(MouseEvent event) {
        Label clickedLabel = (Label) event.getSource();
        if (selectedLabel != null) {
            selectedLabel.setStyle("-fx-font-size: 16px; -fx-border-color: gray; -fx-padding: 10;"); // Remove selection style from previous label but keep the text style
        }
        selectedLabel = clickedLabel;
        selectedLabel.setStyle("-fx-font-size: 16px; -fx-border-color: gray; -fx-padding: 10; -fx-background-color: lightblue;"); // Apply selection style

        // Retrieve recipeId from the label and show the recipe overlay
        int recipeId = (int) clickedLabel.getUserData();
        homeController.showRecipeOverlay(recipeId);
    }

    public void addItemToInbox(Node item) {
        contentBox.getChildren().add(item);
    }

    private Stage stage;

    @FXML
    private void goHome() {
        if (stage != null) {
            stage.close();
        }

        if (homeController != null) {
            homeController.loadPage(1); // Assuming loadPage(1) navigates to the home page
        }
    }
}
