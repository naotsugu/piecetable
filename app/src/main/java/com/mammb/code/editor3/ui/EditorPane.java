/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.model.Content;
import com.mammb.code.editor3.model.Edit;
import com.mammb.code.editor3.model.TextSlice;
import com.mammb.code.editor3.model.TextSource;
import com.mammb.code.editor3.model.TextView;
import com.mammb.code.editor3.model.specifics.ContentImpl;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    /** The stage. */
    private final Stage stage;

    /** The text pane. */
    private TextPane textPane;


    /**
     * Constructor.
     */
    public EditorPane(Stage stage) {

        this.stage = stage;

        Content content = new ContentImpl();
        content.handle(Edit.insert(0, "hello"));
        TextSource textSource = new TextSource(content);
        TextSlice textSlice = new TextSlice(textSource);
        textPane = new TextPane(stage, new TextView(textSlice));

        getChildren().add(textPane);
    }


    public void showScene() {
        Scene scene = new Scene(this, 800, 480);
        stage.setScene(scene);
        stage.show();
    }

}
