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
package com.mammb.code.editor3.ui.behavior;

import com.mammb.code.editor3.ui.UiCaret;

/**
 * CaretBehavior.
 * @author Naotsugu Kobayashi
 */
public class CaretBehavior {

    /** The ui caret. */
    private final UiCaret caret;

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;


    /**
     * Constructor.
     * @param caret the ui caret
     * @param scrollBehavior the scroll behavior
     */
    public CaretBehavior(UiCaret caret, ScrollBehavior scrollBehavior) {
        this.caret = caret;
        this.scrollBehavior = scrollBehavior;
    }


    /**
     * Move the caret to the right.
     */
    public void right() {
        caret.right();
    }


    /**
     * Move the caret to the left.
     */
    public void left() {
        caret.left();
    }


    /**
     * Move the caret up.
     */
    public void up() {
        caret.up();
    }


    /**
     * Move the caret down.
     */
    public void down() {
        caret.down();
    }


    /**
     * Move the caret to the end of row.
     */
    public void end() {
        caret.end();
    }


    /**
     * Move the caret to the end of row.
     */
    public void home() {
        caret.home();
    }


    private void scrollIfEdged() {

        if (caret.physicalY() < 0) {
            int n = (int) Math.ceil(Math.abs(caret.physicalY() - caret.height()) / caret.height());
            for (int i = 0; i <= n; i++) {
                scrollBehavior.scrollNext();
            }
        }
    }

}
