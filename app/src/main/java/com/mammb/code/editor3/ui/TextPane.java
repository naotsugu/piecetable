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

import com.mammb.code.editor3.model.TextView;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import java.util.Objects;

/**
 * TextPane.
 * @author Naotsugu Kobayashi
 */
public class TextPane extends StackPane {

    private final TextFlow textFlow = new TextFlow();

    private final TextView model;

    /**
     * Constructor.
     * @param model the text view model
     */
    public TextPane(TextView model) {

        this.model = Objects.requireNonNull(model);
        getChildren().add(textFlow);
        boundsInParentProperty().addListener(this::handleBoundsChanged);

        setOnDragOver(DragDrop.dragOverHandler());
        setOnDragDropped(DragDrop.droppedHandler(System.out::println));
    }


    private void handleBoundsChanged(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        if (oldValue.getHeight() != newValue.getHeight()) {
            int maxRows = (int) Math.ceil(getBoundsInParent().getHeight() / Texts.height);
            model.setupMaxRows(maxRows);
            textFlow.set(model.text());
        }
    }

}
