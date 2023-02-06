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

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * DragDrop.
 * @author Naotsugu Kobayashi
 */
public class DragDrop {

    /**
     * Get the drag over handler.
     * @return the drag over handler
     */
    static EventHandler<? super DragEvent> dragOverHandler() {
        return event -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        };
    }

    /**
     * Get the dropped handler.
     * @param consumer the path consumer
     * @return the dropped handler
     */
    static EventHandler<? super DragEvent> droppedHandler(Consumer<Path> consumer) {
        return event -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                Optional<Path> maybePath = board.getFiles().stream().findFirst().map(File::toPath);
                if (maybePath.isPresent()) {
                    Path path = maybePath.get();
                    consumer.accept(path);
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        };
    }

}
