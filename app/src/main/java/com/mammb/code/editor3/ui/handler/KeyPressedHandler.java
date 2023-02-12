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

import com.mammb.code.editor3.ui.TextPane;
import com.mammb.code.editor3.ui.util.Keys;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import java.io.File;

import static com.mammb.code.editor3.ui.util.FileChoosers.fileChooseOpen;

/**
 * KeyPressedHandler.
 * @author Naotsugu Kobayashi
 */
public class KeyPressedHandler implements EventHandler<KeyEvent> {

    /** The text pane. */
    private final TextPane textPane;


    /**
     * Constructor.
     * @param textPane the text pane
     */
    private KeyPressedHandler(TextPane textPane) {
        this.textPane = textPane;
    }


    /**
     * Create a new {@code KeyPressedHandler}.
     * @param textPane the text pane
     * @return a new {@code KeyPressedHandler}
     */
    public static EventHandler<KeyEvent> of(TextPane textPane) {
        return new KeyPressedHandler(textPane);
    }


    @Override
    public void handle(KeyEvent e) {

        boolean handled = handleKeyCombination(e);
        if (handled) return;

        switch (e.getCode()) {
            case LEFT       -> System.out.println("left");
            case RIGHT      -> System.out.println("right");
            case UP         -> textPane.caret().up();
            case DOWN       -> textPane.caret().down();
            case HOME       -> System.out.println("home");
            case END        -> System.out.println("end");
            case PAGE_UP    -> System.out.println("pu");
            case PAGE_DOWN  -> System.out.println("pd");
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
            File file = fileChooseOpen(textPane.stage(), null);
            if (file != null) {
                textPane.open(file.toPath());
            }
            return true;
        }

        return false;
    }

}
