module com.cookbook {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.sql;
  requires jbcrypt;
  requires transitive javafx.graphics;
	requires java.desktop;
  requires javafx.base;

  opens com.cookbook to javafx.fxml;
  exports com.cookbook;
}
