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

import com.mammb.code.editor3.lang.Functions;
import com.mammb.code.editor3.ui.control.FlatDialog;
import com.mammb.code.editor3.ui.util.Colors;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * OverlayPane.
 * @author Naotsugu Kobayashi
 */
public class Overlay extends StackPane {

    /**
     * Constructor.
     */
    public Overlay() {
        setAlignment(Pos.CENTER);
        Color c = Colors.background;
        setBackground(new Background(
            new BackgroundFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.7),
                CornerRadii.EMPTY, Insets.EMPTY)));
        setFocused(false);
        setVisible(false);
        setAccessibleRole(AccessibleRole.NODE);
        setOnKeyPressed(Event::consume);
        setOnKeyTyped(Event::consume);
    }


    /**
     * Confirmation will be performed.
     * @param contentText the content text
     * @param ok the action if OK is selected
     * @param cancel the action if cancel is selected
     */
    public void confirm(String contentText, Runnable ok, Runnable cancel) {
        FlatDialog dialog = FlatDialog.confirmOf(contentText, ok, Functions.and(cancel, this::clear));
        getChildren().add(dialog);
        setVisible(true);
        requestFocus();
    }


    /**
     * Confirmation will be performed.
     * @param contentText the content text
     * @param ok the action if OK is selected
     */
    public void confirm(String contentText, Runnable ok) {
        confirm(contentText, ok, Functions.empty());
    }

    /**
     * Clear overlay.
     */
    public void clear() {
        setVisible(false);
        setFocused(false);
        getChildren().clear();
    }

}
