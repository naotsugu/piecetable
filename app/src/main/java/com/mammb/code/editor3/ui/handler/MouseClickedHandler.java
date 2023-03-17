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
package com.mammb.code.editor3.ui.handler;

import com.mammb.code.editor3.ui.behavior.CaretBehavior;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * MouseClickedHandler.
 * @author Naotsugu Kobayashi
 */
public class MouseClickedHandler implements EventHandler<MouseEvent> {

    /** The caret behavior. */
    private final CaretBehavior caretBehavior;


    /**
     * Constructor.
     * @param caretBehavior the CaretBehavior
     */
    private MouseClickedHandler(CaretBehavior caretBehavior) {
        this.caretBehavior = caretBehavior;
    }


    /**
     * Create a new {@code EventHandler<MouseEvent>}.
     * @param caretBehavior the caret behavior
     * @return a new {@code EventHandler<MouseEvent>}
     */
    public static EventHandler<MouseEvent> of(CaretBehavior caretBehavior) {
        return new MouseClickedHandler(caretBehavior);
    }


    @Override
    public void handle(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            caretBehavior.click(event.getX(), event.getY());
        } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            caretBehavior.clickDouble(event.getX(), event.getY());
        }
    }

}
