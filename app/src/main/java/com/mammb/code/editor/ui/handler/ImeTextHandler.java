/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.editor.ui.handler;

import com.mammb.code.editor.ui.behavior.ImeBehavior;
import javafx.event.EventHandler;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodTextRun;
import java.util.stream.Collectors;

/**
 * ImeTextChangedHandler.
 * @author Naotsugu Kobayashi
 */
public class ImeTextHandler implements EventHandler<InputMethodEvent> {

    /** The ime behavior. */
    private final ImeBehavior behavior;


    /**
     * Constructor.
     * @param behavior the ime behavior
     */
    public ImeTextHandler(ImeBehavior behavior) {
        this.behavior = behavior;
    }


    /**
     * Create a new {@code EventHandler<InputMethodEvent>}.
     * @return a new {@code EventHandler<InputMethodEvent>}
     */
    public static EventHandler<InputMethodEvent> of(ImeBehavior behavior) {
        return new ImeTextHandler(behavior);
    }


    @Override
    public void handle(InputMethodEvent event) {

        if (event.getCommitted().length() > 0) {
            behavior.committed(event.getCommitted());
        } else if (!event.getComposed().isEmpty()) {
            behavior.composed(event.getComposed().stream()
                .map(InputMethodTextRun::getText).collect(Collectors.joining()));
        } else {
            behavior.exit();
        }

    }

}
