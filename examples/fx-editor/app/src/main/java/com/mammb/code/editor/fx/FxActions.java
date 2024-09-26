/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.fx;

import com.mammb.code.editor.core.Action;
import javafx.scene.input.*;
import java.util.function.Predicate;
import static javafx.scene.input.KeyCode.*;

/**
 * The FxActions.
 * @author Naotsugu Kobayashi
 */
public abstract class FxActions {

    public static Action of(KeyEvent e) {
        e.consume();

        if (e.getCode() == RIGHT) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_RIGHT)
                : Action.of(Action.Type.CARET_RIGHT);
        else if (e.getCode() == LEFT) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_LEFT)
                : Action.of(Action.Type.CARET_LEFT);
        else if (e.getCode() == UP) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_UP)
                : Action.of(Action.Type.CARET_UP);
        else if (e.getCode() == DOWN) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_DOWN)
                : Action.of(Action.Type.CARET_DOWN);
        else if (e.getCode() == HOME || SC_HOME.match(e)) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_HOME)
                : Action.of(Action.Type.HOME);
        else if (e.getCode() == END || SC_END.match(e)) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_END)
                : Action.of(Action.Type.END);
        else if (e.getCode() == PAGE_UP) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_PAGE_UP)
                : Action.of(Action.Type.PAGE_UP);
        else if (e.getCode() == PAGE_DOWN) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_PAGE_DOWN)
                : Action.of(Action.Type.PAGE_DOWN);
        else if (e.getCode() == ESCAPE) return Action.of(Action.Type.ESC);
        else if (e.getCode() == DELETE) return Action.of(Action.Type.DELETE);
        else if (e.getCode() == BACK_SPACE) return Action.of(Action.Type.BACK_SPACE);
        else if (SC_C.match(e)) return Action.of(Action.Type.COPY);
        else if (SC_V.match(e)) return Action.of(Action.Type.PASTE);
        else if (SC_X.match(e)) return Action.of(Action.Type.CUT);
        else if (SC_Z.match(e)) return Action.of(Action.Type.UNDO);
        else if (SC_Y.match(e) || SC_SZ.match(e)) return Action.of(Action.Type.REDO);
        else if (SC_O.match(e)) return Action.of(Action.Type.OPEN);
        else if (SC_S.match(e)) return Action.of(Action.Type.SAVE);
        else if (SC_SA.match(e)) return Action.of(Action.Type.SAVE_AS);
        else if (SC_N.match(e)) return Action.of(Action.Type.NEW);

        else {
            if (keyInput.test(e)) {
                int ascii = e.getCharacter().getBytes()[0];
                if (ascii < 32 || ascii == 127) { // 127:DEL
                    if (ascii != 9 && ascii != 10 && ascii != 13) { // 9:HT 10:LF 13:CR
                        return Action.of(Action.Type.EMPTY);
                    }
                }
                String ch = (ascii == 13) // 13:CR
                        ? "\n"
                        : e.getCharacter();

                return ch.isEmpty()
                        ? Action.of(Action.Type.EMPTY)
                        : Action.of(Action.Type.TYPED, ch);
            }
        }
        return Action.of(Action.Type.EMPTY);
    }

    private static final KeyCombination SC_C = new KeyCharacterCombination("c", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_V = new KeyCharacterCombination("v", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_X = new KeyCharacterCombination("x", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_Z = new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_Y = new KeyCharacterCombination("y", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_SZ= new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    private static final KeyCombination SC_END  = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_HOME = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_N = new KeyCharacterCombination("n", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_S = new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_SA= new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private static final Predicate<KeyEvent> controlKeysFilter = e ->
            System.getProperty("os.name").toLowerCase().startsWith("windows")
                    ? !e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0
                    : !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();

    private static final Predicate<KeyEvent> keyInput = e -> e.getEventType() == KeyEvent.KEY_TYPED &&
            !(e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
                    e.getCode().isArrowKey()    || e.getCode().isModifierKey() ||
                    e.getCode().isMediaKey()    || !controlKeysFilter.test(e) ||
                    e.getCharacter().isEmpty());
}
