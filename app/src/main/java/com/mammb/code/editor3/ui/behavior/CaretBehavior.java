package com.mammb.code.editor3.ui.behavior;

import com.mammb.code.editor3.ui.UiCaret;

public class CaretBehavior {

    /** The ui caret. */
    private final UiCaret caret;


    /**
     * Constructor.
     * @param caret the ui caret
     */
    public CaretBehavior(UiCaret caret) {
        this.caret = caret;
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

}
