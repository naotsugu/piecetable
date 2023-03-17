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

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.HitInfo;

/**
 * MouseDraggedHandler.
 * @author Naotsugu Kobayashi
 */
public class MouseDraggedHandler implements EventHandler<MouseEvent> {

    /**
     * Constructor.
     */
    private MouseDraggedHandler() {

    }


    /**
     * Create a new {@code EventHandler<MouseEvent>}.
     * @return a new {@code EventHandler<MouseEvent>}
     */
    public static EventHandler<MouseEvent> of() {
        return new MouseDraggedHandler();
    }


    @Override
    public void handle(MouseEvent event) {
//        if (imePalette.getImeOn()) {
//            return;
//        }
//        if (!e.getButton().equals(MouseButton.PRIMARY)) return;
//        HitInfo hit = textFlow.hitTest(textFlow.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY())));
//        screenBuffer.moveCaret(hit.getInsertionIndex());
//        if (!selection.isDragging()) {
//            selection.startDrag(hit.getInsertionIndex());
//        }
    }

}
