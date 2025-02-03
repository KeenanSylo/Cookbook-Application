-- Table for users
CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    DisplayName VARCHAR(100),
    Password VARCHAR(100) NOT NULL
);

-- Table for recipes
CREATE TABLE Recipes (
    RecipeID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    ShortDescription TEXT,
    Ingredients TEXT,
    DetailedDescription TEXT,
    Portions INT,
    UserID INT,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Table for comments
CREATE TABLE Comments (
    CommentID INT AUTO_INCREMENT PRIMARY KEY,
    RecipeID INT,
    UserID INT,
    Comment TEXT,
    Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Table for tags
CREATE TABLE Tags (
    TagID INT AUTO_INCREMENT PRIMARY KEY,
    TagName VARCHAR(50) UNIQUE NOT NULL
);

-- Table for recipe-tag relationship
CREATE TABLE RecipeTags (
    RecipeID INT,
    TagID INT,
    PRIMARY KEY (RecipeID, TagID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (TagID) REFERENCES Tags(TagID)
);

-- Table for favorites
CREATE TABLE Favorites (
    UserID INT,
    RecipeID INT,
    PRIMARY KEY (UserID, RecipeID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);

-- Table for weekly dinner lists
CREATE TABLE WeeklyDinnerLists (
    ListID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT,
    ListName VARCHAR(100) NOT NULL,
    WeekStartDate DATE NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Table for weekly dinner list items
CREATE TABLE WeeklyDinnerListItems (
    ListItemID INT AUTO_INCREMENT PRIMARY KEY,
    ListID INT,
    RecipeID INT,
    DayOfWeek INT,
    FOREIGN KEY (ListID) REFERENCES WeeklyDinnerLists(ListID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);

-- Table for shopping lists
CREATE TABLE ShoppingLists (
    ListID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT,
    ListName VARCHAR(100) NOT NULL,
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Table for shopping list items
CREATE TABLE ShoppingListItems (
    ListItemID INT AUTO_INCREMENT PRIMARY KEY,
    ListID INT,
    RecipeID INT,
    IngredientName VARCHAR(255) NOT NULL,
    Quantity DECIMAL(10,2),
    Unit VARCHAR(50),
    FOREIGN KEY (ListID) REFERENCES ShoppingLists(ListID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);

-- Table for messages
CREATE TABLE Messages (
    MessageID INT AUTO_INCREMENT PRIMARY KEY,
    SenderID INT,
    ReceiverID INT,
    RecipeID INT,
    Message TEXT,
    Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (SenderID) REFERENCES Users(UserID),
    FOREIGN KEY (ReceiverID) REFERENCES Users(UserID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);




----------------------Update the Table----------------------
CREATE TABLE Ingredients (
    IngredientID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE RecipeIngredients (
    RecipeIngredientID INT AUTO_INCREMENT PRIMARY KEY,
    RecipeID INT,
    IngredientID INT,
    Quantity DECIMAL(10, 2) DEFAULT 1,  -- Default quantity to 1
    Unit VARCHAR(50) DEFAULT 'unit',    -- Default unit to 'unit'
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID)
);

-- Extract unique ingredients and insert into Ingredients table
INSERT IGNORE INTO Ingredients (Name)
SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(Ingredients, ',', numbers.n), ',', -1))
FROM Recipes, 
    (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) numbers
WHERE CHAR_LENGTH(Ingredients) - CHAR_LENGTH(REPLACE(Ingredients, ',', '')) >= numbers.n - 1
GROUP BY 1;

-- Insert into RecipeIngredients
INSERT INTO RecipeIngredients (RecipeID, IngredientID, Quantity, Unit)
SELECT 
    RecipeID, 
    Ingredients.IngredientID, 
    1 AS Quantity,   -- Default quantity
    'unit' AS Unit   -- Default unit
FROM Recipes,
    Ingredients,
    (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) numbers
WHERE 
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(Recipes.Ingredients, ',', numbers.n), ',', -1)) = Ingredients.Name
    AND CHAR_LENGTH(Recipes.Ingredients) - CHAR_LENGTH(REPLACE(Recipes.Ingredients, ',', '')) >= numbers.n - 1;



--emir
CREATE TABLE SentRecipes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    recipe_id INT,
    message TEXT,
    date_time DATETIME,
    FOREIGN KEY (sender_id) REFERENCES Users(UserID),
    FOREIGN KEY (receiver_id) REFERENCES Users(UserID),
    FOREIGN KEY (recipe_id) REFERENCES Recipes(RecipeID)
);


--abdi
ALTER TABLE Tags ADD COLUMN UserID INT;
ALTER TABLE Tags ADD FOREIGN KEY (UserID) REFERENCES Users(UserID);


--aakrish
CREATE TABLE WeekDayCombinations (
    WeekDayID INT AUTO_INCREMENT PRIMARY KEY,
    week INT NOT NULL,
    day VARCHAR(20) NOT NULL,
    UNIQUE (week, day)
);

CREATE TABLE WeeklyDinnerLists (
    WeekDayID INT NOT NULL,
    RecipeID INT NOT NULL,
    UserID INT NOT NULL,
    RecipeName VARCHAR(255) NOT NULL,
    FOREIGN KEY (WeekDayID) REFERENCES WeekDayCombinations(WeekDayID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);