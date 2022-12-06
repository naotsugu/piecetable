package com.mammb.code.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.nio.file.Path;
import com.mammb.code.piecetable.PieceTable;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        TextPane textPane = new TextPane();
        Scene scene = new Scene(new StackPane(textPane), 640, 480);
        stage.setScene(scene);
        stage.show();

        PieceTable pieceTable = PieceTable.of(Path.of("../README.md"));
        textPane.setText(pieceTable.toString());
        textPane.setCaretPosition(34);
    }

    public static void main(String[] args) {
        launch();
    }

}
