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
package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.Colors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * LeftGarter.
 * @author Naotsugu Kobayashi
 */
public class LeftGarter extends StackPane {

    static final double MIN_WIDTH = 50;

    public LeftGarter(RowsPanel rowsPanel) {
        setPrefWidth(MIN_WIDTH);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
        setBackground(new Background(new BackgroundFill(Colors.panel, null, null)));
        getChildren().add(rowsPanel);
    }

}
