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
package com.mammb.code.editor.ui.util;

import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import java.util.function.Predicate;

/**
 * Key utilities.
 * @author Naotsugu Kobayashi
 */
public class Keys {

    public static final KeyCombination SC_C = new KeyCharacterCombination("c", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_V = new KeyCharacterCombination("v", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_X = new KeyCharacterCombination("x", KeyCombination.SHORTCUT_DOWN);

    public static final KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_S = new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_SA= new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    public static final KeyCombination SC_Z = new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_Y = new KeyCharacterCombination("y", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_SZ= new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    public static final KeyCombination SC_N = new KeyCharacterCombination("n", KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SC_A = new KeyCharacterCombination("a", KeyCombination.SHORTCUT_DOWN);

    public static final KeyCombination SC_W = new KeyCharacterCombination("w", KeyCombination.SHORTCUT_DOWN);

    public static final Predicate<KeyEvent> controlKeysFilter = e ->
        System.getProperty("os.name").startsWith("Windows")
            ? !e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0
            : !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();

}
