/*
 * Copyright 2022-2024 the original author or authors.
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

import com.mammb.code.editor.ui.behavior.EditBehavior;
import com.mammb.code.editor.ui.util.Keys;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

/**
 * KeyTypedHandler.
 * @author Naotsugu Kobayashi
 */
public class KeyTypedHandler implements EventHandler<KeyEvent> {

    /** The input behavior. */
    private final EditBehavior editBehavior;


    /**
     * Constructor.
     */
    private KeyTypedHandler(EditBehavior editBehavior) {
        this.editBehavior = editBehavior;
    }


    /**
     * Create a new {@code EventHandler<KeyEvent>}.
     * @return a new {@code EventHandler<KeyEvent>}
     */
    public static EventHandler<KeyEvent> of(EditBehavior editBehavior) {
        return new KeyTypedHandler(editBehavior);
    }


    @Override
    public void handle(KeyEvent e) {

        if (e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
            e.getCode().isArrowKey() || e.getCode().isModifierKey() ||
            e.getCode().isMediaKey() || !Keys.controlKeysFilter.test(e) ||
            e.getCharacter().length() == 0) {
            return;
        }
        int ascii = e.getCharacter().getBytes()[0];
        if (ascii < 32 || ascii == 127) {
            // 127:DEL
            if (ascii != 9 && ascii != 10 && ascii != 13) {
                // 9:HT 10:LF 13:CR
                return;
            }
        }

        editBehavior.input(e.getCharacter());

    }

}
