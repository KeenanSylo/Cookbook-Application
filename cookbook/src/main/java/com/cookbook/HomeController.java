package com.cookbook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HomeController {
    @FXML
    private InboxController inboxController;

    public void addNewInboxItem() {
        Label newItem = new Label("New Inbox Item");
        inboxController.addItemToInbox(newItem);
    }

    public static HomeController instance; // Singleton instance

    public int currentUserId; // ID of the currently logged-in user
    @FXML
    public int selectedRecipeId;
    @FXML
    public Button addList;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        updateUserName();
        loadPage(currentPage); // Load the page after currentUserId is set
    }

    public Main main;

    public static final String DB_URL = "jdbc:mysql://localhost:3306/Projectcourse";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "minecraft.co.id";
    public static final int RECIPES_PER_PAGE = 18;

    public int currentPage = 1; // Current page number
    public String currentQuery = "SELECT * FROM Recipes"; // Default query
    public List<Object> currentQueryParameters = new ArrayList<>(); // Parameters for the current query
    private Stage stage; // This is where the injected stage will be stored

    @FXML
    private BorderPane mainBorderPane;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Your logout method
    public void logout() {
        if (stage != null) {
            stage.close();
        } else {
            System.err.println("Stage is null");
        }

        // Assuming you have a main object to switch to login
        // Make sure 'main' is properly initialized and injected as well
        if (main != null) {
            main.switchToLogin();
        } else {
            System.err.println("Main is null");
        }
    }

    @FXML
    public TextField searchNameField;
    @FXML
    public TextField searchIngredientsField;
    @FXML
    public TextField searchTagsField;
    @FXML
    public Button viewFavoritesButton;
    @FXML
    public StackPane centerStackPane; // StackPane for overlay management

    @FXML
    public VBox recipeDetailsOverlay; // Overlay for detailed information

    @FXML
    public FlowPane recipePane;
    @FXML
    public Label welcomeLabel;

    @FXML
    public Label recipeName;
    @FXML
    public Label recipeShortDescription;
    @FXML
    public VBox recipeIngredients;
    @FXML
    public Text recipeDetailedDescription;
    @FXML
    public ImageView userProfileImage;

    @FXML
    public Text recipePortions;
    @FXML
    public VBox tagList; // VBox to display tags
    @FXML
    public VBox commentList; // VBox to display comments
    @FXML
    public Label createdBy;
    @FXML
    public TextArea commentInput;
    @FXML
    public Button addCommentButton;
    @FXML
    public TextField portionInput; // TextField for new portion size
    @FXML
    public Button viewDinner;
    @FXML
    private AnchorPane sendRecipePopup;

    @FXML
    private TextField recipientUsername;

    @FXML
    private TextArea messageText;

    @FXML
    private Button sendButton;
    public String nameRecipe;

    public HomeController() {
    }

    public static HomeController getInstance() {
        if (instance == null) {
            instance = new HomeController();
        }
        return instance;
    }

    @FXML
    void initialize() {
        loadPage(currentPage); // Load the first page
    }

    public void setMain(Main main) {
        this.main = main;
    }

    void loadPage(int page) {
        if (recipePane == null) {
            System.err.println("Error: recipePane is null.");
            return;
        }

        recipePane.getChildren().clear(); // Clear the pane
        currentPage = page; // Update the current page

        // Ensure LIMIT and OFFSET are applied correctly
        String queryWithPagination = currentQuery + " LIMIT ? OFFSET ?";

        // Set the limit and offset
        List<Object> queryParameters = new ArrayList<>(currentQueryParameters);
        queryParameters.add(RECIPES_PER_PAGE); // Number of recipes per page
        queryParameters.add((currentPage - 1) * RECIPES_PER_PAGE); // Offset for pagination

        fetchAndDisplayRecipes(queryWithPagination, queryParameters); // Fetch and display recipes
    }

    // Getter for currentUserId
    public int getCurrentUserId() {
        return currentUserId;
    }

    @FXML
    public void hideRecipeOverlay() {
        recipeDetailsOverlay.setVisible(false); // Hide the overlay
    }

    public void fetchAndDisplayRecipes(String query, List<Object> parameters) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Bind the parameters to the prepared statement
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i)); // Correct parameter index
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                recipePane.getChildren().clear(); // Clear existing children before adding new ones

                while (resultSet.next()) {

                    addRecipeToPane(resultSet); // Add new components to the pane
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error while fetching recipes: " + e.getMessage());
        }
    }

    public void addRecipeToPane(ResultSet resultSet) {
        try {
            // Create the recipe box
            BorderPane recipeBox = new BorderPane();
            recipeBox.setPrefSize(120, 150); // Original size
            recipeBox.setMaxWidth(120);
            recipeBox.setStyle(
                    "-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-radius: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

            // Extract the recipe data while the ResultSet is open
            String recipeNameStr = resultSet.getString("Name");
            String shortDescription = resultSet.getString("ShortDescription");
            int recipeId = resultSet.getInt("RecipeID");

            // Set the text for the recipe name
            Text recipeNameText = new Text(recipeNameStr);
            recipeNameText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
            recipeNameText.setTextAlignment(TextAlignment.CENTER);
            recipeNameText.setWrappingWidth(120);

            // Add the recipe name to the top of the box
            recipeBox.setTop(recipeNameText);

            // Tooltip for the short description
            Tooltip descriptionTooltip = new Tooltip(shortDescription);
            Tooltip.install(recipeBox, descriptionTooltip);

            // Use an array to encapsulate the `isFavorite` state
            final boolean[] isFavorite = { isRecipeFavorite(currentUserId, recipeId) };

            // Star button for marking as favorite
            Button starButton = new Button(isFavorite[0] ? "★" : "☆");
            starButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            starButton.setStyle(isFavorite[0] ? "-fx-text-fill: yellow; -fx-background-color: transparent;"
                    : "-fx-text-fill: black; -fx-background-color: transparent;");

            // Set action for toggling favorite status
            starButton.setOnAction(event -> {
                if (isFavorite[0]) {
                    removeFromFavorites(currentUserId, recipeId);
                    starButton.setText("☆");
                    starButton.setStyle("-fx-text-fill: black; -fx-background-color: transparent;");
                } else {
                    addToFavorites(currentUserId, recipeId);
                    starButton.setText("★");
                    starButton.setStyle("-fx-text-fill: yellow; -fx-background-color: transparent;");
                }
                isFavorite[0] = !isFavorite[0];
            });

            // Create an HBox to hold the star button at the bottom right
            HBox bottomRight = new HBox();
            bottomRight.setAlignment(Pos.BOTTOM_RIGHT);
            bottomRight.getChildren().add(starButton);
            bottomRight.setPadding(new Insets(10, 10, 10, 10));

            // Add the bottomRight HBox to the recipe box at the bottom
            recipeBox.setBottom(bottomRight);

            // Recipe box click event for detailed information
            recipeBox.setOnMouseClicked(event -> {
                showRecipeDetails(recipeId); // Show overlay with details
                this.passRecipeName(recipeNameStr); // Pass the recipe name to AddToDinner

            });

            // Add the recipe box to the main pane
            recipePane.getChildren().add(recipeBox);

        } catch (SQLException e) {
            System.err.println("Error accessing recipe data: " + e.getMessage());
        }
    }

    public void passRecipeName(String recipeName) {
        this.nameRecipe = recipeName;
        System.out.println("Recipe name set to: " + this.nameRecipe);

    }

    @FXML
    public void nextPage() {
        int totalPages = getTotalPages(); // Get total number of pages

        if (currentPage < totalPages) { // Only go to the next page if we're not at the last page
            currentPage++; // Increment page number
            loadPage(currentPage); // Load the new page
        }
    }

    @FXML
    public void previousPage() {
        if (currentPage > 1) { // Only go to the previous page if we're not at the first page
            currentPage--; // Decrement page number
            loadPage(currentPage); // Load the new page
        }
    }

    public void searchRecipes() {
        currentPage = 1; // Reset page number
        currentQueryParameters.clear(); // Clear existing parameters

        String query = buildSearchQuery(currentQueryParameters); // Build query with parameters
        currentQuery = query; // Set new current query

        loadPage(currentPage); // Load the first page of search results
    }

    public String buildSearchQuery(List<Object> parameters) {
        List<String> conditions = new ArrayList<>();
        parameters.clear(); // Clear existing parameters

        // Check the text fields for input
        String name = searchNameField.getText().trim();
        String ingredients = searchIngredientsField.getText().trim();
        String tags = searchTagsField.getText().trim();

        // If a recipe name is specified, add it to the conditions
        if (!name.isEmpty()) {
            conditions.add("Name LIKE ?");
            parameters.add("%" + name + "%");
        }

        // If ingredients are specified, add them to the conditions
        if (!ingredients.isEmpty()) {
            String[] ingredientList = ingredients.split(",");
            List<String> ingredientConditions = new ArrayList<>();
            for (String ingredient : ingredientList) {
                ingredientConditions.add(
                        "EXISTS (SELECT 2 FROM RecipeIngredients WHERE RecipeIngredients.RecipeID = Recipes.RecipeID AND RecipeIngredients.IngredientID = (SELECT IngredientID FROM Ingredients WHERE Name = ?))");
                parameters.add(ingredient.trim());
            }
            conditions.add("(" + String.join(" AND ", ingredientConditions) + ")");
        }

        // If tags are specified, add them to the conditions
        if (!tags.isEmpty()) {
            String[] tagList = tags.split(",");
            List<String> tagConditions = new ArrayList<>();
            for (String tag : tagList) {
                tagConditions.add(
                        "EXISTS (SELECT 1 FROM RecipeTags WHERE RecipeTags.RecipeID = Recipes.RecipeID AND RecipeTags.TagID = (SELECT TagID FROM Tags WHERE TagName = ?))");
                parameters.add(tag.trim());
            }
            conditions.add("(" + String.join(" AND ", tagConditions) + ")");
        }

        // Construct the final query
        String query = "SELECT * FROM Recipes";

        if (!conditions.isEmpty()) {
            query += " WHERE " + String.join(" AND ", conditions);
        }

        return query;
    }

    public void setWelcomeMessage(String userName) {
        welcomeLabel.setText("Welcome, " + userName + "!");
    }

    // Method to open the "Add Recipe" window
    @FXML
    public void addRecipe() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addRecipe.fxml"));
        FlowPane recipeView = loader.load();

        AddRecipeController addRecipeController = loader.getController();
        addRecipeController.setHomeController(this); // Set HomeController instance

        recipePane.getChildren().clear();
        recipePane.getChildren().add(recipeView);
    }

    @FXML
    public void inbox() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("inbox.fxml"));
        FlowPane recipeView = loader.load();

        InboxController inboxcontroller = loader.getController();
        inboxcontroller.setHomeController(this); // Set HomeController instance

        recipePane.getChildren().clear();
        recipePane.getChildren().add(recipeView);
    }

    @FXML
    public void addToList(ActionEvent event) throws IOException {
        try {
            // Load the content of dinner.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dinner.fxml"));
            Parent dinnerContent = loader.load();

            // Get the controller instance
            AddToDinner addToDinnerController = loader.getController();

            // Set the HomeController instance in AddToDinner controller
            addToDinnerController.setHomeController(this);
            addToDinnerController.receiverecipeName(nameRecipe);

            // Create a new stage for the dinner content
            Stage dinnerStage = new Stage();
            dinnerStage.setTitle("Dinner");
            dinnerStage.setScene(new Scene(dinnerContent));

            // Show the new stage
            dinnerStage.show();
        } catch (IOException e) {
            System.err.println("Error loading dinner.fxml: " + e.getMessage());
        }
    }

    @FXML
    public void viewDinnerList(ActionEvent event) throws IOException {
        // Load the new FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("weeklyDinner.fxml"));
        Parent root = loader.load();

        // Get the controller instance from the loader
        WeeklyDinnerController weeklyDinnerController = loader.getController();

        // Pass the homeController instance to the WeeklyDinnerController
        weeklyDinnerController.setHomeController(this);

        // Get the current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Create a new scene with the same dimensions as the current scene
        Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());

        // Set the new scene
        stage.setScene(scene);
        stage.show();
    }

    public boolean isRecipeFavorite(int userId, int recipeId) {
        String query = "SELECT COUNT(*) FROM Favorites WHERE UserID = ? AND RecipeID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, recipeId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // True if there's at least one row
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking favorite status: " + e.getMessage());
        }
        return false; // Default to false
    }

    public void addToFavorites(int userId, int recipeId) {
        String query = "INSERT INTO Favorites (UserID, RecipeID) VALUES (?, ?)";

        // Logging the query with bound parameters
        System.out.println("Executing query: " + query);
        System.out.println("With parameters: UserID = " + userId + ", RecipeID = " + recipeId);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameters
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, recipeId);

            // Execute the insert statement
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding to favorites: " + e.getMessage());
        }
    }

    public void removeFromFavorites(int userId, int recipeId) {
        String query = "DELETE FROM Favorites WHERE UserID = ? AND RecipeID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, recipeId);
            preparedStatement.executeUpdate(); // Execute the delete
        } catch (SQLException e) {
            System.err.println("Error removing from favorites: " + e.getMessage());
        }
    }

    public void loadFavorites() {
        if (recipePane == null) {
            System.err.println("Error: recipePane is null.");
            return;
        }

        recipePane.getChildren().clear(); // Clear the existing content

        String query = "SELECT r.* FROM Recipes r JOIN Favorites f ON r.RecipeID = f.RecipeID WHERE f.UserID = ?";
        List<Object> parameters = new ArrayList<>();
        parameters.add(currentUserId);

        fetchAndDisplayRecipes(query, parameters); // Fetch and display the favorite recipes
    }

    public int getTotalRecipeCount() {
        int totalRecipes = 0;
        String query = "SELECT COUNT(*) FROM Recipes";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                totalRecipes = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recipe count: " + e.getMessage());
        }

        return totalRecipes;
    }

    public int getTotalPages() {
        int totalRecipes = getTotalRecipeCount();
        return (int) Math.ceil((double) totalRecipes / RECIPES_PER_PAGE);
    }

    public void loadRecipes() {
        String query = "SELECT RecipeID, Name, ShortDescription FROM Recipes";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            recipePane.getChildren().clear(); // Clear existing content

            while (rs.next()) {
                BorderPane recipeBox = createRecipeBox(rs.getInt("RecipeID"), rs.getString("Name"),
                        rs.getString("ShortDescription"));
                recipePane.getChildren().add(recipeBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BorderPane createRecipeBox(int recipeId, String name, String shortDescription) {
        BorderPane recipeBox = new BorderPane();

        // Basic details
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("Arial", 14));
        Label shortDescLabel = new Label(shortDescription);
        recipeBox.setTop(nameLabel);
        recipeBox.setBottom(shortDescLabel);

        // Click handler to show detailed information in the overlay
        recipeBox.setOnMouseClicked((MouseEvent event) -> {
            showRecipeOverlay(recipeId); // Show overlay with details

        });

        return recipeBox;
    }

    public void showRecipeOverlay(int recipeId) {
        // Base query to get recipe details
        String query = "SELECT r.*, u.DisplayName FROM Recipes r "
                + "JOIN Users u ON r.UserID = u.UserID WHERE r.RecipeID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Update basic recipe information
                    recipeName.setText(rs.getString("Name"));
                    recipeShortDescription.setText(rs.getString("ShortDescription"));
                    recipeDetailedDescription.setText(rs.getString("DetailedDescription"));
                    recipePortions.setText("Portions: " + rs.getInt("Portions"));

                    createdBy.setText("Created by: " + rs.getString("DisplayName"));

                    // Get the ingredients from the RecipeIngredients table
                    loadRecipeIngredients(recipeId); // New method to load recipe ingredients

                    // Load other details (tags, comments)
                    loadRecipeTags(recipeId, currentUserId);
                    loadRecipeComments(recipeId);

                    // Show the recipe details overlay
                    recipeDetailsOverlay.setVisible(true);
                } else {
                    System.err.println("Error: No data found for RecipeID: " + recipeId);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error accessing recipe details: " + e.getMessage());
        }
    }

    private void loadRecipeIngredients(int recipeId) {
        // Query to fetch ingredients with quantities and units
        String query = "SELECT i.Name AS IngredientName, ri.Quantity, ri.Unit FROM RecipeIngredients ri "
                + "JOIN Ingredients i ON ri.IngredientID = i.IngredientID "
                + "WHERE ri.RecipeID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                recipeIngredients.getChildren().clear(); // Clear existing ingredients
                while (rs.next()) {
                    String ingredient = rs.getString("IngredientName");
                    BigDecimal quantity = rs.getBigDecimal("Quantity");
                    String unit = rs.getString("Unit");

                    // Create a label with the formatted ingredient details
                    String ingredientDetails = String.format("%s: %s %s", ingredient, quantity, unit);
                    Label ingredientLabel = new Label(ingredientDetails);

                    recipeIngredients.getChildren().add(ingredientLabel); // Add to the VBox
                }

            } catch (SQLException e) {
                System.err.println("Error loading recipe ingredients: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    private void showRecipeDetails(int recipeId) {
        // Base query to get recipe details along with the creator's display name
        String query = "SELECT r.*, u.DisplayName FROM Recipes r "
                + "JOIN Users u ON r.UserID = u.UserID WHERE r.RecipeID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Assign the selected recipe ID to `selectedRecipeId`
                    selectedRecipeId = recipeId;

                    // Update UI components with the basic recipe information
                    recipeName.setText(rs.getString("Name"));
                    recipeShortDescription.setText(rs.getString("ShortDescription"));
                    recipeDetailedDescription.setText(rs.getString("DetailedDescription"));
                    recipePortions.setText("Portions: " + rs.getInt("Portions"));
                    createdBy.setText("Created by: " + rs.getString("DisplayName"));

                    // Load the recipe ingredients from the RecipeIngredients table
                    loadRecipeIngredients(recipeId);

                    // Load tags and comments
                    loadRecipeTags(recipeId, currentUserId);
                    loadRecipeComments(recipeId);

                    // Show the recipe details overlay
                    recipeDetailsOverlay.setVisible(true);
                } else {
                    System.err.println("Error: No data found for RecipeID: " + recipeId);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error accessing recipe details: " + e.getMessage());
        }
    }

    private void loadRecipeTags(int recipeId, int currentUserId) {
        String query = "SELECT t.TagName FROM RecipeTags rt JOIN Tags t ON rt.TagID = t.TagID " +
                "WHERE rt.RecipeID = ? AND (t.UserID IS NULL OR t.UserID = ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, recipeId);
            ps.setInt(2, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                tagList.getChildren().clear(); // Clear existing tags
                while (rs.next()) {
                    Text tagText = new Text("#" + rs.getString("TagName"));
                    tagList.getChildren().add(tagText);
                }

            } catch (SQLException e) {
                System.err.println("Error loading recipe tags: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    private void loadRecipeComments(int recipeId) {
        String query = "SELECT c.CommentID, c.Comment, c.Timestamp, u.UserID, u.DisplayName AS CommentedBy " +
                "FROM Comments c JOIN Users u ON c.UserID = u.UserID " +
                "WHERE c.RecipeID = ? ORDER BY c.Timestamp DESC";

        List<CommentDetails> comments = new ArrayList<>(); // Store comments details

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                commentList.getChildren().clear(); // Clear existing comments
                while (rs.next()) {
                    CommentDetails commentDetail = new CommentDetails();
                    commentDetail.setCommentId(rs.getInt("CommentID"));
                    commentDetail.setComment(rs.getString("Comment"));
                    commentDetail.setTimestamp(rs.getString("Timestamp"));
                    commentDetail.setUserId(rs.getInt("UserID"));
                    commentDetail.setCommentedBy(rs.getString("CommentedBy"));

                    comments.add(commentDetail); // Add to comments list

                    VBox commentBox = new VBox();
                    commentBox.setPadding(new Insets(5));
                    commentBox.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1; -fx-border-radius: 5;");

                    Text commentedBy = new Text(commentDetail.getCommentedBy());
                    commentedBy.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    Text commentText = new Text(commentDetail.getComment());
                    Text timestampText = new Text("Posted on: " + commentDetail.getTimestamp());

                    // Edit and Delete Buttons (only show for user's own comments)
                    HBox buttonBox = new HBox();
                    if (commentDetail.getUserId() == currentUserId) {
                        Button editButton = new Button("Edit");
                        editButton.setOnAction(e -> editComment(commentDetail));
                        buttonBox.getChildren().add(editButton);

                        Button deleteButton = new Button("Delete");
                        deleteButton.setOnAction(e -> deleteComment(commentDetail));
                        buttonBox.getChildren().add(deleteButton);
                    }

                    buttonBox.setSpacing(10);
                    VBox.setMargin(buttonBox, new Insets(5, 0, 0, 0));

                    commentBox.getChildren().addAll(commentedBy, commentText, timestampText, buttonBox);
                    commentList.getChildren().add(commentBox);
                }
            } catch (SQLException e) {
                System.err.println("Error loading recipe comments: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    // Custom class to hold comment details
    class CommentDetails {
        private int commentId;
        private String comment;
        private String timestamp;
        private int userId;
        private String commentedBy;

        // Getters and setters
        public int getCommentId() {
            return commentId;
        }

        public void setCommentId(int commentId) {
            this.commentId = commentId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getCommentedBy() {
            return commentedBy;
        }

        public void setCommentedBy(String commentedBy) {
            this.commentedBy = commentedBy;
        }
    }

    private void editComment(CommentDetails commentDetail) {
        TextInputDialog dialog = new TextInputDialog(commentDetail.getComment());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText(null);
        dialog.setContentText("Please edit your comment:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(editedComment -> {
            String query = "UPDATE Comments SET Comment = ?, Timestamp = NOW() WHERE CommentID = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, editedComment);
                ps.setInt(2, commentDetail.getCommentId());

                int updatedRows = ps.executeUpdate();
                if (updatedRows > 0) {
                    System.out.println("Comment updated successfully!");
                    // Reload comments to reflect the edit
                    loadRecipeComments(selectedRecipeId);
                } else {
                    System.err.println("Failed to update comment. No rows affected.");
                }

            } catch (SQLException e) {
                System.err.println("Error updating comment: " + e.getMessage());
            }
        });
    }

    private void deleteComment(CommentDetails commentDetail) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this comment?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM Comments WHERE CommentID = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setInt(1, commentDetail.getCommentId());

                int deletedRows = ps.executeUpdate();
                if (deletedRows > 0) {
                    System.out.println("Comment deleted successfully!");
                    // Reload comments to reflect the deletion
                    loadRecipeComments(selectedRecipeId);
                } else {
                    System.err.println("Failed to delete comment. No rows affected.");
                }

            } catch (SQLException e) {
                System.err.println("Error deleting comment: " + e.getMessage());
            }
        }
    }

    @FXML
    public void addComment() {
        if (commentInput == null || commentInput.getText().trim().isEmpty()) {
            showAlert("Cannot add an empty comment", Alert.AlertType.WARNING);
            return;
        }

        int recipeId = selectedRecipeId; // Get the current recipe ID
        if (recipeId <= 0) { // Validate the recipe ID
            System.err.println("Invalid recipe ID. Cannot add comment.");
            return; // Stop if the recipe ID is invalid
        }

        String commentText = commentInput.getText().trim(); // Get the cleaned comment text

        String query = "INSERT INTO Comments (RecipeID, UserID, Comment, Timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement ps = conn.prepareStatement(query)) {

            // Bind the recipe ID, current user ID, and comment text
            ps.setInt(1, recipeId);
            ps.setInt(2, currentUserId); // Ensure currentUserId is valid
            ps.setString(3, commentText);

            // Execute the insert query
            ps.executeUpdate();

            System.out.println("Comment added successfully!");

            // Clear the comment input after successful addition
            commentInput.clear();

            // Reload comments to reflect the new addition
            loadRecipeComments(recipeId);

        } catch (SQLException e) {
            System.err.println("Error adding comment: " + e.getMessage());
            // Additional error handling if needed
        }
    }

    @FXML
    public void recalculateIngredients() {
        try {
            // Get the new portion size from the input field
            int newPortionSize = Integer.parseInt(portionInput.getText().trim());

            if (newPortionSize <= 0) {
                showAlert("Portion size must be a positive integer.", Alert.AlertType.ERROR);
                return;
            }

            // Get the original portion size
            String originalPortionsText = recipePortions.getText();
            String[] portionParts = originalPortionsText.split(":");
            if (portionParts.length != 2) {
                showAlert("Invalid format for original portion size.", Alert.AlertType.ERROR);
                return;
            }
            int originalPortionSize = Integer.parseInt(portionParts[1].trim());

            // Calculate the ratio for ingredient adjustments
            double ratio = (double) newPortionSize / originalPortionSize;

            // Adjust ingredient quantities based on the ratio
            adjustIngredients(ratio);

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for the portion size.", Alert.AlertType.ERROR);
        }
    }

    public Map<String, BigDecimal> originalQuantities = new HashMap<>();

    public void adjustIngredients(double ratio) {
        for (Node node : recipeIngredients.getChildren()) {
            if (node instanceof Label) {
                Label ingredientLabel = (Label) node;
                String ingredientText = ingredientLabel.getText();

                // Check if the ingredient text contains the expected format
                if (!ingredientText.contains(":")) {
                    showAlert("Invalid ingredient format 1: " + ingredientText, Alert.AlertType.ERROR);
                    continue; // Skip processing this invalid ingredient
                }

                // Extract ingredient details
                String[] parts = ingredientText.split(":");
                if (parts.length != 2) {
                    showAlert("Invalid ingredient format 2: " + ingredientText, Alert.AlertType.ERROR);
                    continue; // Skip processing this invalid ingredient
                }

                String name = parts[0].trim();
                String quantityAndUnit = parts[1].trim();

                // Extract quantity and unit
                String[] quantityUnitParts = quantityAndUnit.split(" ");
                BigDecimal originalQuantity;
                if (quantityUnitParts.length != 2) {
                    showAlert("Invalid ingredient format 3: " + ingredientText, Alert.AlertType.ERROR);
                    continue; // Skip processing this invalid ingredient
                }

                BigDecimal newQuantity = null;
                try {
                    if (originalQuantities.containsKey(name)) {
                        originalQuantity = originalQuantities.get(name);
                    } else {
                        originalQuantity = new BigDecimal(quantityUnitParts[0]);
                        originalQuantities.put(name, originalQuantity);
                    }
                    String unit = quantityUnitParts[1];

                    // Recalculate the new quantity
                    newQuantity = originalQuantity.multiply(BigDecimal.valueOf(ratio)).setScale(2,
                            RoundingMode.HALF_UP);

                    // Update the label with the recalculated quantity
                    String updatedText = String.format("%s: %s %s", name,
                            newQuantity.stripTrailingZeros().toPlainString(), unit);
                    ingredientLabel.setText(updatedText);
                } catch (NumberFormatException e) {
                    showAlert("Error processing ingredient: " + ingredientText, Alert.AlertType.ERROR);
                } catch (Exception e) {
                    showAlert("An error occurred while processing ingredient: " + ingredientText,
                            Alert.AlertType.ERROR);
                }
            }
        }
    }

    public void showAlert(String message, @SuppressWarnings("exports") Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void sendToAnother() {
        sendRecipePopup.setVisible(true);
    }

    @FXML
    public void closeSendRecipePopup() {
        sendRecipePopup.setVisible(false);
    }

    public int getCurrentRecipeId() {
        // Returns the ID of the currently selected recipe
        return selectedRecipeId;
    }

    @FXML
    public void sendRecipe(ActionEvent event) {
        int recipeId = getCurrentRecipeId(); // Fetch the current recipe ID
        String message = messageText.getText().trim(); // Message to send
        String recipientUsernameStr = recipientUsername.getText().trim(); // Recipient's username
        int senderID = this.getCurrentUserId(); // Sender's user ID

        // Get the recipient's user ID from the database
        int recipientID = getUserIdByUsername(recipientUsernameStr);

        // Check if a valid recipient user ID was retrieved
        if (recipientID == -1) {
            System.out.println("Invalid recipient username.");
            return; // Exit if no valid recipient
        }

        // Print all necessary details to the terminal
        System.out.println("Message: " + message);
        System.out.println("Recipient Username: " + recipientUsernameStr);
        System.out.println("Recipient User ID: " + recipientID);
        System.out.println("Recipe ID: " + recipeId);
        System.out.println("Sender ID: " + senderID);

        // Insert the sending details into the SentRecipes table
        insertSentRecipe(senderID, recipientID, recipeId, message);

        sendRecipePopup.setVisible(false);
    }

    private int getUserIdByUsername(String username) {
        String query = "SELECT UserID FROM Users WHERE Username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("UserID");
                } else {
                    System.err.println("User not found: " + username);
                    return -1; // Return -1 or any indicator for not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching user ID: " + e.getMessage());
            return -1;
        }
    }

    private void insertSentRecipe(int senderId, int receiverId, int recipeId, String message) {
        String query = "INSERT INTO SentRecipes (sender_id, receiver_id, recipe_id, message, date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, senderId);
            preparedStatement.setInt(2, receiverId);
            preparedStatement.setInt(3, recipeId);
            preparedStatement.setString(4, message);
            preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // Set current time as
                                                                                          // timestamp

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Recipe successfully sent and recorded with current datetime.");
            } else {
                System.err.println("Failed to record the sent recipe with datetime.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while inserting into SentRecipes: " + e.getMessage());
        }
    }

    public void updateUserName() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT username FROM Users WHERE userId = ?")) {

            preparedStatement.setInt(1, currentUserId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String userName = resultSet.getString("username");
                    setWelcomeMessage(userName);
                } else {
                    System.err.println("User not found for ID: " + currentUserId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching user name: " + e.getMessage());
        }
    }
}
