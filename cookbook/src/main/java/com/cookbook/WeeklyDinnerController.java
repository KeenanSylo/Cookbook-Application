package com.cookbook;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class WeeklyDinnerController {
    @FXML
    private TableView<RecipeForDay> table;
    @FXML
    private TableColumn<RecipeForDay, String> dayColumn;

    @FXML
    private TableColumn<RecipeForDay, String> recipeColumn;

    @FXML
    private FlowPane viewDinner;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private Button backHome;

    @FXML
    private Button shopping;

    @FXML
    private Button shoppingList;

    private HomeController homeController;
    private Stage stage;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        ObservableList<String> weekList = FXCollections.observableArrayList();
        for (int i = 1; i <= 52; i++) {
            weekList.add("Week " + i);
        }
        choiceBox.setItems(weekList);

        // Set cell value factories for each TableColumn
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        recipeColumn.setCellValueFactory(new PropertyValueFactory<>("recipeName"));
    }

    @FXML
    private void backToHome() {
        if (stage != null) {
            stage.close();
        }
        if (homeController != null) {
            int userId = homeController.getCurrentUserId();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("test.fxml"));
                Parent homeRoot = loader.load();

                Stage currentStage = (Stage) ((Node) backHome).getScene().getWindow();
                Scene homeScene = new Scene(homeRoot);

                currentStage.setScene(homeScene);
                currentStage.show();

                HomeController controller = loader.getController();
                controller.setCurrentUserId(userId);
                controller.loadPage(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleChoiceBoxSelection() {
        String selectedWeek = choiceBox.getValue();
        String url = "jdbc:mysql://localhost:3306/Projectcourse";
        String user = "root";
        String pass = "minecraft.co.id";
        int userId = homeController.getCurrentUserId();

        if (selectedWeek != null) {
            int weekNumber = Integer.parseInt(selectedWeek.replace("Week ", ""));
            ObservableList<RecipeForDay> recipesForWeek = FXCollections.observableArrayList();

            try (Connection conn = DriverManager.getConnection(url, user, pass);
                    PreparedStatement statement = conn.prepareStatement(
                            "SELECT WeekDayCombinations.day, WeeklyDinnerLists.recipeName " +
                                    "FROM WeekDayCombinations " +
                                    "JOIN WeeklyDinnerLists ON WeekDayCombinations.WeekDayID = WeeklyDinnerLists.WeekDayID " +
                                    "WHERE WeekDayCombinations.week = ? AND WeeklyDinnerLists.UserID = ? " +
                                    "ORDER BY WeekDayCombinations.day")) {

                statement.setInt(1, weekNumber);
                statement.setInt(2, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String day = resultSet.getString("day");
                        String recipeName = resultSet.getString("recipeName");
                        recipesForWeek.add(new RecipeForDay(day, recipeName));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            table.setItems(recipesForWeek);
        }
    }

    @FXML
    private void createShoppingList() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void viewShoppingList() {
        String selectedWeek = choiceBox.getValue();
        if (selectedWeek != null) {
            int weekNumber = Integer.parseInt(selectedWeek.replace("Week ", ""));
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("shopping.fxml"));
                Parent shoppingListView = loader.load();

                ShoppingController shoppingController = loader.getController();
                shoppingController.setHomeController(homeController);
                shoppingController.setSelectedWeek(weekNumber); // Pass the selected week to ShoppingController

                Stage shoppingStage = new Stage();
                shoppingStage.setTitle("Shopping List");

                Scene scene = new Scene(shoppingListView);
                shoppingStage.setScene(scene);

                shoppingStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class RecipeForDay {
        private final StringProperty day;
        private final StringProperty recipeName;

        public RecipeForDay(String day, String recipeName) {
            this.day = new SimpleStringProperty(day);
            this.recipeName = new SimpleStringProperty(recipeName);
        }

        public String getDay() {
            return day.get();
        }

        public void setDay(String day) {
            this.day.set(day);
        }

        public StringProperty dayProperty() {
            return day;
        }

        public String getRecipeName() {
            return recipeName.get();
        }

        public void setRecipeName(String recipeName) {
            this.recipeName.set(recipeName);
        }

        public StringProperty recipeNameProperty() {
            return recipeName;
        }
    }
    public void setMain(Main main) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'setMain'");
  }
}

