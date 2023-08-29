package io.github.cmuphil.tetrad.fx;

import io.github.cmuphil.tetrad.fx.gui.TetradFx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(TetradFx.getInstance().getPane());
        primaryStage.setScene(scene);
        primaryStage.setTitle("ScrollPane with Pane");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
