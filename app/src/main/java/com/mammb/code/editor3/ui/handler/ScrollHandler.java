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

import com.mammb.code.editor3.model.behavior.ScrollBehavior;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

/**
 * ScrollHandler.
 * @author Naotsugu Kobayashi
 */
public class ScrollHandler implements EventHandler<ScrollEvent> {

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;


    /**
     * Constructor.
     * @param scrollBehavior the scroll behavior
     */
    private ScrollHandler(ScrollBehavior scrollBehavior) {
        this.scrollBehavior = scrollBehavior;
    }


    /**
     * Create a new {@code EventHandler<ScrollEvent>}.
     * @param scrollBehavior the scroll behavior
     * @return a new {@code EventHandler<ScrollEvent>}
     */
    public static EventHandler<ScrollEvent> of(ScrollBehavior scrollBehavior) {
        return new ScrollHandler(scrollBehavior);
    }


    @Override
    public void handle(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            if (e.getDeltaY() > 2)  scrollBehavior.prev(2);
            else if (e.getDeltaY() > 0)  scrollBehavior.prev(1);
            else if (e.getDeltaY() < -2) scrollBehavior.next(2);
            else if (e.getDeltaY() < 0)  scrollBehavior.next(1);
        }
    }

}
