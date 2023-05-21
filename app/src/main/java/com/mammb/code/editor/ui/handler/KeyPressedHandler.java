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

import com.mammb.code.editor.ui.behavior.CaretBehavior;
import com.mammb.code.editor.ui.behavior.ConfBehavior;
import com.mammb.code.editor.ui.behavior.EditBehavior;
import com.mammb.code.editor.ui.behavior.FileBehavior;
import com.mammb.code.editor.ui.behavior.ImeBehavior;
import com.mammb.code.editor.ui.behavior.ScrollBehavior;
import com.mammb.code.editor.ui.util.Keys;
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

    /** The edit behavior. */
    private final EditBehavior editBehavior;

    /** The edit behavior. */
    private final ImeBehavior imeBehavior;

    /** The file choose behavior. */
    private final FileBehavior fileBehavior;

    /** The conf behavior. */
    private final ConfBehavior confBehavior;


    /**
     * Constructor.
     * @param caretBehavior the caret behavior
     * @param scrollBehavior the scroll behavior
     * @param editBehavior the edit behavior
     * @param imeBehavior the ime behavior
     * @param fileBehavior the file choose behavior
     * @param confBehavior the conf behavior
     */
    private KeyPressedHandler(
            CaretBehavior caretBehavior,
            ScrollBehavior scrollBehavior,
            EditBehavior editBehavior,
            ImeBehavior imeBehavior,
            FileBehavior fileBehavior,
            ConfBehavior confBehavior) {
        this.caretBehavior = caretBehavior;
        this.scrollBehavior = scrollBehavior;
        this.editBehavior = editBehavior;
        this.imeBehavior = imeBehavior;
        this.fileBehavior = fileBehavior;
        this.confBehavior = confBehavior;
    }


    /**
     * Create a new {@code KeyPressedHandler}.
     * @param caretBehavior the caret behavior
     * @param scrollBehavior the scroll behavior
     * @param editBehavior the edit behavior
     * @param imeBehavior the ime behavior
     * @param fileBehavior the file choose behavior
     * @param confBehavior the conf behavior
     * @return a new {@code KeyPressedHandler}
     */
    public static EventHandler<KeyEvent> of(
            CaretBehavior caretBehavior,
            ScrollBehavior scrollBehavior,
            EditBehavior editBehavior,
            ImeBehavior imeBehavior,
            FileBehavior fileBehavior,
            ConfBehavior confBehavior) {
        return new KeyPressedHandler(
            caretBehavior, scrollBehavior, editBehavior, imeBehavior, fileBehavior, confBehavior);
    }


    @Override
    public void handle(KeyEvent e) {

        if (imeBehavior.doing()) return;

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
            case PAGE_UP    -> scrollBehavior.pageUp(true);
            case PAGE_DOWN  -> scrollBehavior.pageDown(true);
            case DELETE     -> editBehavior.delete();
            case BACK_SPACE -> editBehavior.backspace();
            case F1         -> editBehavior.dump();
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
            fileBehavior.open();
            return true;
        }
        if (Keys.SC_S.match(e)) {
            fileBehavior.save();
            return true;
        }
        if (Keys.SC_SA.match(e)) {
            fileBehavior.saveAs();
            return true;
        }

        // clipboard operations
        if (Keys.SC_C.match(e)) {
            editBehavior.copyToClipboard();
            return true;
        }
        if (Keys.SC_V.match(e)) {
            editBehavior.pasteFromClipboard();
            return true;
        }
        if (Keys.SC_X.match(e)) {
            editBehavior.cutToClipboard();
            return true;
        }

        // undo redo
        if (Keys.SC_Z.match(e)) {
            editBehavior.undo();
            return true;
        }
        if (Keys.SC_Y.match(e) || Keys.SC_SZ.match(e)) {
            editBehavior.redo();
            return true;
        }

        if (Keys.SC_W.match(e)) {
            confBehavior.toggleTextWrap();
            return true;
        }
        return false;
    }


    /**
     * Handle shift down key combination for selection.
     * @param e the key event
     */
    private void handleSelection(KeyEvent e) {
        if (e.isShiftDown() && isShiftSelectCombination(e)) {
            caretBehavior.select();
        } else if(!e.isShiftDown() && isShiftSelectCombination(e)) {
            caretBehavior.clearSelect();
        }
    }


    /**
     * Get whether the shift down key combination.
     * @param e the key event
     * @return {@code true}, if whether the shift down key combination
     */
    private boolean isShiftSelectCombination(KeyEvent e) {
        final KeyCode code = e.getCode();
        return code == KeyCode.LEFT    || code ==  KeyCode.RIGHT ||
               code == KeyCode.UP      || code ==  KeyCode.DOWN ||
               code == KeyCode.HOME    || code ==  KeyCode.END ||
               code == KeyCode.PAGE_UP || code ==  KeyCode.PAGE_DOWN;
    }

}
