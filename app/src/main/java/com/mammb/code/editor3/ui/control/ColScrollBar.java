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
package com.mammb.code.editor3.ui.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;

/**
 * ColScrollBar.
 * @author Naotsugu Kobayashi
 */
public class ColScrollBar extends StackPane {

    /** The height of scroll bar. */
    private final double HEIGHT = 8;

    /** The thumb. */
    private final ScrollThumb thumb = ScrollThumb.rowOf(HEIGHT);

    /** The max value of scroll bar. */
    private final DoubleProperty max = new SimpleDoubleProperty(0);

    /** The value of scroll bar. */
    private final DoubleProperty value = new SimpleDoubleProperty(0);


    /**
     * Constructor.
     */
    public ColScrollBar() {

    }

}
