package com.mammb.code.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        newWindow(stage);
    }


    private void newWindow(Stage stage) {
        TextPane textPane = new TextPane(stage);
        Scene scene = new Scene(new StackPane(textPane), 800, 480);
        scene.setOnKeyPressed(this::handleKeyPressed);
        Image image = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icon/icon512.png")));
        stage.getIcons().add(image);
        stage.setScene(scene);
        stage.show();
    }

    private void handleKeyPressed(KeyEvent e) {
        if (Keys.SC_N.match(e)) {
            newWindow(new Stage());
            e.consume();
        }
    }


    public static void main(String[] args) {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");
        launch();
    }

}
