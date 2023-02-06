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

import com.mammb.code.editor2.model.TextView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.util.List;
import java.util.Objects;

/**
 * TextPane.
 * @author Naotsugu Kobayashi
 */
public class TextPane extends StackPane {

    private TextView textView;

    private TextFlow textFlow = new TextFlow();

    /**
     * Constructor.
     * @param textView the text view
     */
    public TextPane(TextView textView) {

        this.textView = Objects.requireNonNull(textView);
        getChildren().add(textFlow);
        boundsInParentProperty().addListener((observable, ov, nv) -> fillText());

        setOnDragOver(DragDrop.dragOverHandler());
        setOnDragDropped(DragDrop.droppedHandler(System.out::println));
    }

    void fillText() {
        int maxRows = (int) Math.ceil(getBoundsInParent().getHeight() / Texts.height);
        textView.setupMaxRows(maxRows);
        textFlow.set(List.of(new Text(textView.string())));
    }

}
