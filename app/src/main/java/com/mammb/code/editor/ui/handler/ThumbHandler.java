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
package com.mammb.code.editor.ui.handler;

import com.mammb.code.editor.lang.EventListener;
import com.mammb.code.editor.ui.behavior.ScrollBehavior;
import com.mammb.code.editor.ui.control.ScrollBarChange;

/**
 * ThumbHandler.
 * @author Naotsugu Kobayashi
 */
public class ThumbHandler implements EventListener<ScrollBarChange> {

    /** The scroll behavior. */
    private final ScrollBehavior behavior;


    /**
     * Constructor.
     * @param behavior the behavior
     */
    private ThumbHandler(ScrollBehavior behavior) {
        this.behavior = behavior;
    }


    /**
     * Create a new {@code ThumbHandler}.
     * @param behavior the behavior
     * @return a new {@code ThumbHandler}
     */
    public static EventListener<ScrollBarChange> of(ScrollBehavior behavior) {
        return new ThumbHandler(behavior);
    }


    @Override
    public void handle(ScrollBarChange event) {

        if (event.axis() == ScrollBarChange.Axis.VERTICAL) {
            // row
            switch (event.type()) {
                case THUMB -> handleRowThumbMoved((int) event.distance());
                case TRACK -> handleRowTruckClicked((int) event.distance());
            }

        } else if (event.axis() == ScrollBarChange.Axis.HORIZONTAL) {
            // col
            switch (event.type()) {
                case THUMB -> handleColThumbMoved(event.distance());
                case TRACK -> handleColTruckClicked(event.distance());
            }
        }

    }


    private void handleRowThumbMoved(int delta) {
        if (delta > 0) {
            behavior.scrollNext(delta);
        } else if (delta < 0) {
            behavior.scrollPrev(Math.abs(delta));
        }
    }


    private void handleRowTruckClicked(int delta) {
        if (delta < 0) {
            behavior.pageUp(false);
        } else if (delta > 0) {
            behavior.pageDown(false);
        }
    }


    private void handleColThumbMoved(double delta) {
        behavior.scrollCol(delta);
    }


    private void handleColTruckClicked(double delta) {
        behavior.scrollCol(delta);
    }


}
