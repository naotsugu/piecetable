package com.mammb.code.piecetable.examples;

import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import java.util.function.Predicate;

import static javafx.scene.input.KeyCode.*;

public interface Action {
    Action EMPTY = Action.of(Type.EMPTY);
    Type type();
    String attr();
    long occurredAt();

    record ActionRecord(Action.Type type, String attr, long occurredAt) implements Action { }

    static Action of(Action.Type type) {
        return new ActionRecord(type, "", System.currentTimeMillis());
    }

    static Action of(Action.Type type, String attr) {
        return new ActionRecord(type, attr, System.currentTimeMillis());
    }

    static Action of(KeyEvent e) {
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

    /** The action event type.*/
    enum Type {
        TYPED, DELETE, BACK_SPACE,
        CARET_RIGHT, CARET_LEFT, CARET_UP, CARET_DOWN,
        SELECT_CARET_RIGHT, SELECT_CARET_LEFT, SELECT_CARET_UP, SELECT_CARET_DOWN,
        HOME, SELECT_HOME, END, SELECT_END,
        PAGE_UP, SELECT_PAGE_UP,
        PAGE_DOWN, SELECT_PAGE_DOWN,
        COPY, PASTE, CUT,
        UNDO, REDO,
        OPEN, SAVE, SAVE_AS, NEW,
        EMPTY,
        ;
    }

    KeyCombination SC_C = new KeyCharacterCombination("c", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_V = new KeyCharacterCombination("v", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_X = new KeyCharacterCombination("x", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_Z = new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_Y = new KeyCharacterCombination("y", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_SZ= new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    KeyCombination SC_END  = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_HOME = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_N = new KeyCharacterCombination("n", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_S = new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_SA= new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    Predicate<KeyEvent> controlKeysFilter = e ->
        System.getProperty("os.name").toLowerCase().startsWith("windows")
            ? !e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0
            : !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();

    Predicate<KeyEvent> keyInput = e -> e.getEventType() == KeyEvent.KEY_TYPED &&
        !(e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
            e.getCode().isArrowKey()    || e.getCode().isModifierKey() ||
            e.getCode().isMediaKey()    || !controlKeysFilter.test(e) ||
            e.getCharacter().isEmpty());

}
