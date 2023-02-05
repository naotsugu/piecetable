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
package com.mammb.code.editor2.ui;

import com.mammb.code.editor2.model.BasicContent;
import com.mammb.code.editor2.model.Content;
import com.mammb.code.editor2.model.Edit;
import com.mammb.code.editor2.model.TextSource;
import com.mammb.code.editor2.model.TextView;
import javafx.scene.layout.StackPane;

/**
 * EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    private TextPane textPane;
    private Content content;

    public EditorPane() {
        content = new BasicContent();
        content.handle(new Edit.InsertEdit(0, "hello"));
        TextSource textSource = new TextSource(content);
        textPane = new TextPane(new TextView(textSource));
        getChildren().add(textPane);
    }

}
