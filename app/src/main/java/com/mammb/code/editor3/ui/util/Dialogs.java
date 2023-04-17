package com.mammb.code.editor3.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class Dialogs {

    public static boolean discardChanges() {
        return new Alert(
            Alert.AlertType.CONFIRMATION,
            "Are you sure you want to discard your changes?",
            ButtonType.OK, ButtonType.CANCEL).showAndWait()
            .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }


    static class DialogPane extends StackPane {
        public DialogPane(String contentText) {
            AnchorPane anchorPane = new AnchorPane();
            Label label = new Label(contentText);
            Button ok = new Button("OK");
            Button cancel = new Button("Cancel");
        }
    }

}
