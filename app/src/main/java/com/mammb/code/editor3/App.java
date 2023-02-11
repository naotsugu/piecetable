package com.mammb.code.editor3;

import com.mammb.code.editor3.ui.EditorPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        EditorPane editorPane = new EditorPane();
        Scene scene = new Scene(editorPane, 800, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");
        launch();
    }

}
