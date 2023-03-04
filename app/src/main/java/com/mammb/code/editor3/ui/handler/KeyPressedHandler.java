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
import com.mammb.code.editor3.ui.behavior.FileChooseBehavior;
import com.mammb.code.editor3.ui.behavior.ScrollBehavior;
import com.mammb.code.editor3.ui.util.Keys;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * KeyPressedHandler.
 * @author Naotsugu Kobayashi
 */
public class KeyPressedHandler implements EventHandler<KeyEvent> {

    /** The caret behavior. */
    private final CaretBehavior caretBehavior;

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;

    /** The file choose behavior. */
    private final FileChooseBehavior fileChooseBehavior;


    /**
     * Constructor.
     * @param caretBehavior the caret behavior
     * @param scrollBehavior the scroll behavior
     * @param fileChooseBehavior the file choose behavior
     */
    private KeyPressedHandler(
            CaretBehavior caretBehavior,
            ScrollBehavior scrollBehavior,
            FileChooseBehavior fileChooseBehavior) {
        this.caretBehavior = caretBehavior;
        this.scrollBehavior = scrollBehavior;
        this.fileChooseBehavior = fileChooseBehavior;
    }


    /**
     * Create a new {@code KeyPressedHandler}.
     * @param caretBehavior the caret behavior
     * @param scrollBehavior the scroll behavior
     * @param fileChooseBehavior the file choose behavior
     * @return a new {@code KeyPressedHandler}
     */
    public static EventHandler<KeyEvent> of(
            CaretBehavior caretBehavior,
            ScrollBehavior scrollBehavior,
            FileChooseBehavior fileChooseBehavior) {
        return new KeyPressedHandler(caretBehavior, scrollBehavior, fileChooseBehavior);
    }


    @Override
    public void handle(KeyEvent e) {

        boolean handled = handleKeyCombination(e);
        if (handled) return;

        handleSelection(e);

        switch (e.getCode()) {
            case LEFT       -> caretBehavior.left();
            case RIGHT      -> caretBehavior.right();
            case UP         -> caretBehavior.up();
            case DOWN       -> caretBehavior.down();
            case HOME       -> caretBehavior.home();
            case END        -> caretBehavior.end();
            case PAGE_UP    -> scrollBehavior.pageUp();
            case PAGE_DOWN  -> scrollBehavior.pageDown();
            case DELETE     -> System.out.println("del");
            case BACK_SPACE -> System.out.println("bs");
            case F1         -> System.out.println("f1");
            case ESCAPE     -> System.out.println("esc");
            default -> { }
        }

    }


    /**
     * Handle key combination event.
     * @param e the key event
     * @return {@code true} if event is handled, otherwise {@code false}
     */
    private boolean handleKeyCombination(KeyEvent e) {
        if (Keys.SC_O.match(e)) {
            fileChooseBehavior.open();
            return true;
        }

        return false;
    }


    private void handleSelection(KeyEvent e) {
        if (e.isShiftDown() && isShiftSelectCombination(e)) {
            caretBehavior.select();
        } else if(!e.isShiftDown() && isShiftSelectCombination(e)) {
            caretBehavior.clearSelect();
        }
    }


    private boolean isShiftSelectCombination(KeyEvent e) {
        final KeyCode code = e.getCode();
        return code == KeyCode.LEFT    || code ==  KeyCode.RIGHT ||
               code == KeyCode.UP      || code ==  KeyCode.DOWN ||
               code == KeyCode.HOME    || code ==  KeyCode.END ||
               code == KeyCode.PAGE_UP || code ==  KeyCode.PAGE_DOWN;
    }

}
