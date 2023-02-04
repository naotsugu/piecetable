package com.mammb.code.editor2.ui;

import com.mammb.code.editor2.model.BasicContent;
import com.mammb.code.editor2.model.Content;
import com.mammb.code.editor2.model.ViewPoint;
import javafx.scene.layout.StackPane;

public class EditorPane extends StackPane {

    private TextPane textPane;
    private Content content;

    public EditorPane() {
        content = new BasicContent();
        ViewPoint viewPoint = content.createViewPoint();
        textPane = new TextPane(viewPoint.textView());
        getChildren().add(textPane);
    }

}
