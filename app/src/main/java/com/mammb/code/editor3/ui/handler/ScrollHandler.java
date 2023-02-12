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
import javafx.scene.input.ScrollEvent;

/**
 * ScrollHandler.
 * @author Naotsugu Kobayashi
 */
public class ScrollHandler implements EventHandler<ScrollEvent> {

    /**
     * Constructor.
     */
    private ScrollHandler() {
    }


    /**
     * Create a new {@code EventHandler<ScrollEvent>}.
     * @return a new {@code EventHandler<ScrollEvent>}
     */
    public static EventHandler<ScrollEvent> of() {
        return new ScrollHandler();
    }


    @Override
    public void handle(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            System.out.println("ScrollEvent:" + e.getDeltaY());
        }
    }

}
