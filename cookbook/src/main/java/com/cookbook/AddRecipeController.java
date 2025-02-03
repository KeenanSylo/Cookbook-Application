package com.cookbook;

import java.math.BigDecimal;
import java.sql.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AddRecipeController {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "minecraft.co.id";

  @FXML
  private TextField recipeNameField;

  @FXML
  private TextArea ingredientsField;

  @FXML
  private TextArea shortDescriptionField;

  @FXML
  private TextArea detailedDescriptionField;

  @FXML
  private TextField portion;

  @FXML
  private ComboBox<String> choiceBox;

  @FXML
  private TextField customTagField;

  private HomeController homeController;
  private Stage stage;

  private ArrayList<String> tags = new ArrayList<String>();

  public void setHomeController(HomeController homeController) {
    this.homeController = homeController;
    try {
      if (choiceBox != null) {
        getTags();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      showAlert("Failed to load tags.", Alert.AlertType.ERROR);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  private void initialize() {
    // Initialization code that does not depend on homeController
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
  }

  private void getTags() throws SQLException {
    if (homeController == null) {
      throw new IllegalStateException("HomeController is not set.");
    }

    int currentUserId = homeController.getCurrentUserId();

    try (Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Tags WHERE UserID = ? OR UserID IS NULL")) {
      stmt.setInt(1, currentUserId);
      ResultSet result = stmt.executeQuery();
      while (result.next()) {
        String name = result.getString("TagName");
        choiceBox.getItems().add(name);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
  }

  private int getTagId(String tagName) throws SQLException {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
      String query = "SELECT * FROM tags WHERE TagName = ?";
      try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, tagName);
        ResultSet result = stmt.executeQuery();
        while (result.next()) {
          int tagId = result.getInt("TagID");
          return tagId;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw e;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
    return -1;
  }

  @FXML
  private void chooseTag() {
    tags.add(choiceBox.getSelectionModel().getSelectedItem());
  }

  @FXML
  private void saveRecipe(ActionEvent event) {
    String recipeName = recipeNameField.getText();
    String shortDescription = shortDescriptionField.getText();
    String detailedDescription = detailedDescriptionField.getText();
    String portionSize = portion.getText();

    List<IngredientEntry> ingredientEntries = parseIngredients(ingredientsField.getText());

    int userId = homeController.getCurrentUserId();

    Integer recipeId = insertRecipe(userId, recipeName, shortDescription, detailedDescription, ingredientEntries,
        portionSize);

    if (recipeId != null) {
      System.out.println("RecipeID: " + recipeId + " saved successfully.");

      showAlert("Recipe saved successfully!", Alert.AlertType.INFORMATION);
      if (stage != null) {
        stage.close();
      }
    } else {
      showAlert("Failed to save recipe. Please try again.", Alert.AlertType.ERROR);
    }
  }

  private Integer insertRecipe(int userId, String recipeName, String shortDescription, String detailedDescription,
      List<IngredientEntry> ingredientEntries, String portionSize) {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
      conn.setAutoCommit(false);

      int recipeId;

      try (PreparedStatement recipeStatement = conn.prepareStatement(
          "INSERT INTO Recipes (Name, ShortDescription, DetailedDescription, Portions, UserID) VALUES (?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS)) {
        recipeStatement.setString(1, recipeName);
        recipeStatement.setString(2, shortDescription);
        recipeStatement.setString(3, detailedDescription);
        recipeStatement.setString(4, portionSize);
        recipeStatement.setInt(5, userId);

        recipeStatement.executeUpdate();

        try (ResultSet generatedKeys = recipeStatement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            recipeId = generatedKeys.getInt(1);
          } else {
            throw new SQLException("Failed to generate RecipeID.");
          }
        }
      }

      try (PreparedStatement recipeIngredientStatement = conn.prepareStatement(
          "INSERT INTO RecipeIngredients (RecipeID, IngredientID, Quantity, Unit) VALUES (?, ?, ?, ?)")) {
        for (IngredientEntry entry : ingredientEntries) {
          int ingredientId = ensureIngredientExists(conn, entry.name);

          recipeIngredientStatement.setInt(1, recipeId);
          recipeIngredientStatement.setInt(2, ingredientId);
          recipeIngredientStatement.setBigDecimal(3, entry.quantity);
          recipeIngredientStatement.setString(4, entry.unit);

          recipeIngredientStatement.addBatch();
        }

        recipeIngredientStatement.executeBatch();
      }

      try (PreparedStatement recipeTagStatement = conn.prepareStatement(
          "INSERT INTO RecipeTags (RecipeID, TagID) VALUES (?, ?)")) {
        for (String tag : tags) {
          int tagId = getTagId(tag);

          recipeTagStatement.setInt(1, recipeId);
          recipeTagStatement.setInt(2, tagId);

          recipeTagStatement.addBatch();
        }

        recipeTagStatement.executeBatch();
      }

      conn.commit();
      return recipeId;

    } catch (SQLException e) {
      System.err.println("Error inserting recipe: " + e.getMessage());
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackException) {
          System.err.println("Error during rollback: " + rollbackException.getMessage());
        }
      }
      return null;
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private List<IngredientEntry> parseIngredients(String ingredientText) {
    List<IngredientEntry> ingredientEntries = new ArrayList<>();
    String[] lines = ingredientText.split("\n");

    for (String line : lines) {
      if (line.trim().isEmpty())
        continue;

      String[] parts = line.split("\\s+");
      if (parts.length < 3)
        continue;

      try {
        BigDecimal quantity = new BigDecimal(parts[0]);
        String unit = parts[1];
        String name = Arrays.stream(parts, 2, parts.length)
            .collect(Collectors.joining(" "));

        ingredientEntries.add(new IngredientEntry(name, quantity, unit));

      } catch (NumberFormatException e) {
        System.err.println("Error parsing ingredient quantity: " + e.getMessage());
      }
    }

    return ingredientEntries;
  }

  private void showAlert(String message, Alert.AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setContentText(message);
    alert.showAndWait();
  }

  @FXML
  private void backToHome() {
    if (stage != null) {
      stage.close();
    }

    if (homeController != null) {
      homeController.loadPage(1);
    }
  }

  private static class IngredientEntry {
    String name;
    BigDecimal quantity;
    String unit;

    IngredientEntry(String name, BigDecimal quantity, String unit) {
      this.name = name;
      this.quantity = quantity;
      this.unit = unit;
    }
  }

  public void setMain(Main main) {
    throw new UnsupportedOperationException("Unimplemented method 'setMain'");
  }

  private int ensureIngredientExists(Connection conn, String ingredientName) throws SQLException {
    String checkQuery = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
    String insertQuery = "INSERT INTO Ingredients (Name) VALUES (?)";

    try (PreparedStatement checkStatement = conn.prepareStatement(checkQuery)) {
      checkStatement.setString(1, ingredientName);

      try (ResultSet rs = checkStatement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("IngredientID");
        }
      }
    }

    try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
      insertStatement.setString(1, ingredientName);
      insertStatement.executeUpdate();

      try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return generatedKeys.getInt(1);
        }
      }
    }

    throw new SQLException("Failed to ensure ingredient exists.");
  }

  @FXML
  private void addCustomTag() {
    String tag = customTagField.getText().trim();
    if (!tag.isEmpty()) {
      try {
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Tags (TagName, UserID) VALUES (?, ?)")) {
          stmt.setString(1, tag);
          stmt.setInt(2, homeController.getCurrentUserId());
          stmt.executeUpdate();
          choiceBox.getItems().add(tag);
          customTagField.clear();
        } catch (SQLException e) {
          e.printStackTrace();
          showAlert("Failed to add tag.", Alert.AlertType.ERROR);
        } finally {
          try {
            conn.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Failed to add tag.", Alert.AlertType.ERROR);
      }
    }
  }

  @FXML
  private void cancel(ActionEvent event) {
    if (stage != null) {
      stage.close();
    }
  }
}
