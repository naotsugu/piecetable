package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.model.Content;
import com.mammb.code.editor3.model.Edit;
import com.mammb.code.editor3.model.TextSlice;
import com.mammb.code.editor3.model.TextSource;
import com.mammb.code.editor3.model.TextView;
import com.mammb.code.editor3.model.ContentImpl;
import javafx.scene.layout.StackPane;

/**
 * EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    /** The content. */
    private Content content;

    /** The text pane. */
    private TextPane textPane;

    /**
     * Constructor.
     */
    public EditorPane() {
        content = new ContentImpl();
        content.handle(Edit.insert(0, "hello"));
        TextSource textSource = new TextSource(content);
        TextSlice textSlice = new TextSlice(textSource);
        textPane = new TextPane(new TextView(textSlice));
        getChildren().add(textPane);
    }

}
