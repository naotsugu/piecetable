package com.mammb.code.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        newWindow(stage, "", "");
    }


    private void newWindow(Stage stage, String syntax, String initText) {

        TextPane textPane = new TextPane(stage, syntax, initText);
        textPane.setNewWindowConsumer((mode, text) -> newWindow(new Stage(), mode, text));

        Scene scene = new Scene(textPane, 800, 480);
        scene.setOnKeyPressed(this::handleKeyPressed);

        Image icon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icon/icon512.png")));
        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.show();
    }


    private void handleKeyPressed(KeyEvent e) {
        if (Keys.SC_N.match(e)) {
            newWindow(new Stage(), "", "");
            e.consume();
        }
    }


    public static void main(String[] args) {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");
        launch();
    }

    public static final String banner = """
        =================================================================
          Minimal Editor (v0.0.1)
        -----------------------------------------------------------------
            Ctrl + O           Open file     Ctrl + S           Save
            Ctrl + Shift + S   Save as
            Ctrl + C           Copy          Ctrl + X           Cut
            Ctrl + V           Paste         Ctrl + Z           Undo
            Ctrl + Shift + Z   Redo

            Arrow              Move caret
            Home               Move to head of line
            End                Move to tail of line
            Delete             Delete next character
            BS                 Delete prev character
            End                Move to tail of line
            F1                 Help
        =================================================================
        """;

}
