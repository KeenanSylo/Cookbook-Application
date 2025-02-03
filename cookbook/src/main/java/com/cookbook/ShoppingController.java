package com.cookbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.util.converter.DoubleStringConverter;

public class ShoppingController {

  @FXML
  private Button edit;

  @FXML
  private TableColumn<IngredientDetails, String> ingredients;

  @FXML
  private TableColumn<IngredientDetails, Double> quantity;

  @FXML
  private TableColumn<IngredientDetails, String> unit;

  @FXML
  private TableView<IngredientDetails> tableView;

  @FXML
  private FlowPane shoppingList;

  private ObservableList<IngredientDetails> allIngredients = FXCollections.observableArrayList();

  public HomeController homeController;
  private int selectedWeek;

  @FXML
  public void initialize() {
    // Set cell value factories for each TableColumn
    ingredients.setCellValueFactory(new PropertyValueFactory<>("name"));
    quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    unit.setCellValueFactory(new PropertyValueFactory<>("unit"));

    // Make the TableView editable
    tableView.setEditable(true);

    // Set cell factory to allow editing of quantity column
    quantity.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
    quantity.setOnEditCommit(this::handleQuantityEdit);

    tableView.setItems(allIngredients);
  }

  @FXML
  private void handleRemoveIngredient() {
    IngredientDetails selectedIngredient = tableView.getSelectionModel().getSelectedItem();
    if (selectedIngredient != null) {
      allIngredients.remove(selectedIngredient);
    }
  }

  public void setHomeController(HomeController homeController) {
    this.homeController = homeController;
  }

  public void setSelectedWeek(int week) {
    this.selectedWeek = week;
    loadIngredientsForSelectedWeek();
  }

  private void loadIngredientsForSelectedWeek() {
    int userId = homeController.getCurrentUserId();
    allIngredients.clear();

    Map<String, IngredientDetails> ingredientsMap = new HashMap<>();
    String[] daysOfWeek = { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };

    try (Connection conn = getConnection()) {
      for (String day : daysOfWeek) {
        ArrayList<IngredientDetails> ingredients = loadForWeek(conn, selectedWeek, day, userId);
        mergeIngredients(ingredientsMap, ingredients);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    allIngredients.addAll(ingredientsMap.values());
  }

  private Connection getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/Projectcourse";
    String user = "root";
    String pass = "minecraft.co.id";
    return DriverManager.getConnection(url, user, pass);
  }

  private ArrayList<IngredientDetails> loadForWeek(Connection conn, int weekNumber, String day, int userId)
      throws SQLException {
    String sql = "SELECT i.Name AS IngredientName, ri.Quantity, ri.Unit " +
        "FROM Ingredients i " +
        "JOIN RecipeIngredients ri ON ri.IngredientId = i.IngredientId " +
        "JOIN WeeklyDinnerLists wdl ON wdl.RecipeID = ri.RecipeID " +
        "JOIN WeekDayCombinations wdc ON wdc.WeekDayID = wdl.WeekDayID " +
        "WHERE wdc.week = ? AND wdc.day = ? AND wdl.UserID = ?";
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, weekNumber);
      statement.setString(2, day.toUpperCase());
      statement.setInt(3, userId);

      ResultSet resultSet = statement.executeQuery();
      ArrayList<IngredientDetails> ingredientsList = new ArrayList<>();
      while (resultSet.next()) {
        String ingredientName = resultSet.getString("IngredientName");
        double quantity = resultSet.getDouble("Quantity");
        String unit = resultSet.getString("Unit");
        ingredientsList.add(new IngredientDetails(ingredientName, quantity, unit));
      }
      return ingredientsList;
    }
  }

  private void mergeIngredients(Map<String, IngredientDetails> ingredientsMap,
      ArrayList<IngredientDetails> ingredients) {
    for (IngredientDetails ingredient : ingredients) {
      ingredientsMap.merge(ingredient.getName(), ingredient, (existing, newIngredient) -> {
        existing.setQuantity(existing.getQuantity() + newIngredient.getQuantity());
        return existing;
      });
    }
  }

  private void handleQuantityEdit(TableColumn.CellEditEvent<IngredientDetails, Double> event) {
    IngredientDetails ingredient = event.getRowValue();
    ingredient.setQuantity(event.getNewValue());
  }

  public static class IngredientDetails {
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty quantity;
    private final SimpleStringProperty unit;

    public IngredientDetails(String name, double quantity, String unit) {
      this.name = new SimpleStringProperty(name);
      this.quantity = new SimpleDoubleProperty(quantity);
      this.unit = new SimpleStringProperty(unit);
    }

    public String getName() {
      return name.get();
    }

    public double getQuantity() {
      return quantity.get();
    }

    public void setQuantity(double quantity) {
      this.quantity.set(quantity);
    }

    public String getUnit() {
      return unit.get();
    }

    @Override
    public String toString() {
      return name.get() + " (" + quantity.get() + " " + unit.get() + ")";
    }
  }
}
