package com.minimalide.ide;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"), 16);

        CompilerController controller = new CompilerController();
        Scene scene = new Scene(controller.buildLayout(stage), 960, 680);
        scene.getStylesheets().add(getClass().getResource("/com/minimalide/style.css").toExternalForm());


        scene.setFill(javafx.scene.paint.Color.web("#0e0e0e"));
        stage.setTitle("MinimalIDE");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}