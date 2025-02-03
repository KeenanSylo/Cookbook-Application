package com.cookbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddToDinner implements Initializable {

    @FXML
    private FlowPane addList;

    @FXML
    private Button goBackHome;

    @FXML
    private Button saveDB;

    @FXML
    private ChoiceBox<String> weekDay;

    @FXML
    private ChoiceBox<String> weekNo;

    private HomeController homeController;
    private String nameRecipe;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize weekNo ChoiceBox with values from 1 to 52
        ObservableList<String> weeks = FXCollections.observableArrayList();
        for (int i = 1; i <= 52; i++) {
            weeks.add(String.valueOf("week" + i));
        }
        weekNo.setItems(weeks);

        // Initialize weekDay ChoiceBox with day names
        ObservableList<String> days = FXCollections.observableArrayList(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        weekDay.setItems(days);
    }

    // Add a method to receive the recipe name from HomeController
    public void receiverecipeName(String nameRecipe) {
        this.nameRecipe = nameRecipe;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void saveToDb() {
        if (nameRecipe == null) {
            System.out.println("Recipe name is not set.");
            return; // Exit if nameRecipe is null
        }

        String day = weekDay.getValue();
        String weekStr = weekNo.getValue();
        int week;

        try {
            // Extract the numeric part from the weekStr (e.g., "week12" to "12")
            String filteredWeekStr = weekStr.replaceAll("\\D", "");

            // Check if the filtered string is not empty
            if (filteredWeekStr.isEmpty()) {
                System.out.println("Invalid week number.");
                return; // Exit method if the filtered string is empty
            }

            // Convert the filtered string to an integer
            week = Integer.parseInt(filteredWeekStr);

            // Validate week range
            if (week < 1 || week > 52) {
                System.out.println("Week must be between 1 and 52.");
                return; // Exit method if week is out of range
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid week number.");
            return; // Exit method if week is not a valid integer
        }

        // Validate day name (assuming day names are valid)
        if (!isValidDayName(day)) {
            System.out.println("Invalid day name.");
            return; // Exit method if day name is invalid
        }

        // Get the recipe name from the HomeController
        String recipe = nameRecipe;

        // Get the current user ID from the HomeController
        int userId = homeController.getCurrentUserId();

        // Call the insertDinner method to insert dinner details into the database
        if (insertDinner(userId, week, day.toUpperCase(), recipe)) {
            System.out.println("Dinner saved successfully!");
            // You can add additional logic here if needed
        } else {
            System.out.println("Failed to save dinner.");
            // You can add additional error handling here if needed
        }
        close();
    }

    private boolean isValidDayName(String day) {
        // Assuming valid day names are Monday, Tuesday, Wednesday, etc.
        String[] validDayNames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        for (String validDay : validDayNames) {
            if (validDay.equalsIgnoreCase(day)) {
                return true;
            }
        }
        return false;
    }

    private boolean insertDinner(int userId, int week, String day, String recipe) {
        String url = "jdbc:mysql://localhost:3306/Projectcourse";
        String user = "root";
        String pass = "minecraft.co.id";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);

            try {
                // Retrieve the RecipeID for the given recipe name
                String selectRecipeIdSql = "SELECT RecipeID FROM Recipes WHERE Name = ?";
                PreparedStatement selectRecipeIdStmt = conn.prepareStatement(selectRecipeIdSql);
                selectRecipeIdStmt.setString(1, recipe);
                ResultSet recipeResult = selectRecipeIdStmt.executeQuery();

                int recipeId;
                if (recipeResult.next()) {
                    recipeId = recipeResult.getInt("RecipeID");
                } else {
                    System.out.println("Recipe not found.");
                    return false;
                }

                // Check if the combination of UserID, Week, and RecipeName already exists
                String selectExistingSql = "SELECT * FROM WeeklyDinnerLists WHERE UserID = ? AND WeekDayID IN (SELECT WeekDayID FROM WeekDayCombinations WHERE week = ? AND day = ?) AND RecipeID = ?";
                PreparedStatement selectExistingStmt = conn.prepareStatement(selectExistingSql);
                selectExistingStmt.setInt(1, userId);
                selectExistingStmt.setInt(2, week);
                selectExistingStmt.setString(3, day);
                selectExistingStmt.setInt(4, recipeId);
                ResultSet existingResults = selectExistingStmt.executeQuery();

                // If a record with the same UserID, Week, and RecipeName exists, return false
                if (existingResults.next()) {
                    System.out.println("Recipe already exists for this week.");
                    return false;
                }

                // Insert week and day into WeekDayCombinations if not exists
                String insertWeekDaySql = "INSERT INTO WeekDayCombinations (week, day) " +
                        "VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE WeekDayID = LAST_INSERT_ID(WeekDayID)";
                PreparedStatement insertWeekDayStmt = conn.prepareStatement(insertWeekDaySql,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                insertWeekDayStmt.setInt(1, week);
                insertWeekDayStmt.setString(2, day);
                insertWeekDayStmt.executeUpdate();

                ResultSet generatedKeys = insertWeekDayStmt.getGeneratedKeys();
                int weekDayId;
                if (generatedKeys.next()) {
                    weekDayId = generatedKeys.getInt(1);
                } else {
                    // Retrieve the WeekDayID if the combination already existed
                    String selectWeekDaySql = "SELECT WeekDayID FROM WeekDayCombinations WHERE week = ? AND day = ?";
                    PreparedStatement selectWeekDayStmt = conn.prepareStatement(selectWeekDaySql);
                    selectWeekDayStmt.setInt(1, week);
                    selectWeekDayStmt.setString(2, day);
                    ResultSet rs = selectWeekDayStmt.executeQuery();
                    if (rs.next()) {
                        weekDayId = rs.getInt("WeekDayID");
                    } else {
                        throw new SQLException("Failed to retrieve WeekDayID.");
                    }
                }

                // Insert into WeeklyDinnerLists
                String insertDinnerSql = "INSERT INTO WeeklyDinnerLists (WeekDayID, RecipeID, UserID, RecipeName) " +
                        "VALUES (?, ?, ?, ?)";
                PreparedStatement insertDinnerStmt = conn.prepareStatement(insertDinnerSql);
                insertDinnerStmt.setInt(1, weekDayId);
                insertDinnerStmt.setInt(2, recipeId); // Use the retrieved RecipeID
                insertDinnerStmt.setInt(3, userId);
                insertDinnerStmt.setString(4, recipe);

                int rowsAffected = insertDinnerStmt.executeUpdate();

                conn.commit();

                if (rowsAffected > 0) {
                    System.out.println("Dinner inserted successfully.");
                    return true;
                } else {
                    System.out.println("Failed to insert dinner.");
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error inserting dinner: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void close() {
        Scene scene = addList.getScene();
        Stage stage = (Stage) scene.getWindow();

        // Close the stage
        stage.close();
    }
}