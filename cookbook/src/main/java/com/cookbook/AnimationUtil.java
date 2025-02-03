// AnimationUtil.java
package com.cookbook;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AnimationUtil {

  public static void playStartupAnimation(StackPane root, Stage primaryStage, Runnable onAnimationFinished) {
    // Load the initial image
    Image initialImage = new Image(
        "file:/Users/keenansylo/Documents/1DV508 - Project Course in Computer/working cookbook/cookbook/cookbook/src/main/resources/com/cookbook/image/beef.png");

    // Load the new image to change to
    Image newImage = new Image(
        "file:/Users/keenansylo/Documents/1DV508 - Project Course in Computer/working cookbook/cookbook/cookbook/src/main/resources/com/cookbook/image/salmon.png");

    // Create an ImageView to display the image
    ImageView imageView = new ImageView(initialImage);
    imageView.setFitHeight(300); // Adjust the height as needed
    imageView.setFitWidth(300); // Adjust the width as needed

    // Add the ImageView to the StackPane
    root.getChildren().add(imageView);

    // Create a RotateTransition for the first half of the rotation
    RotateTransition rt1 = new RotateTransition(Duration.millis(1500), imageView);
    rt1.setByAngle(360 * 2); // Half of the total rotation angle
    rt1.setInterpolator(Interpolator.LINEAR);

    // Create a RotateTransition for the second half of the rotation
    RotateTransition rt2 = new RotateTransition(Duration.millis(1500), imageView);
    rt2.setByAngle(360 * 2); // Half of the total rotation angle
    rt2.setInterpolator(Interpolator.LINEAR);

    // Create a PauseTransition to change the image
    PauseTransition pt = new PauseTransition(Duration.millis(1));
    pt.setOnFinished(event -> imageView.setImage(newImage));

    // Create a ScaleTransition to zoom in the image
    ScaleTransition st = new ScaleTransition(Duration.millis(750), imageView);
    st.setByX(2f); // Adjust the zoom factor as needed
    st.setByY(2f); // Adjust the zoom factor as needed
    st.setInterpolator(Interpolator.EASE_IN);

    // Create a FadeTransition to make the image disappear
    FadeTransition ft = new FadeTransition(Duration.millis(2000), imageView);
    ft.setFromValue(1.0);
    ft.setToValue(0.0);
    ft.setInterpolator(Interpolator.EASE_OUT);

    // Create a SequentialTransition to play the RotateTransitions with the pause in between, then the ParallelTransition
    SequentialTransition seqT = new SequentialTransition(
        rt1,
        pt,
        rt2,
        new ParallelTransition(st, ft)
    );
    seqT.setOnFinished(event -> onAnimationFinished.run());

    // Play the SequentialTransition
    seqT.play();
  }

}
