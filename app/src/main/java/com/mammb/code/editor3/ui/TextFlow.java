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

import javafx.geometry.Insets;
import javafx.scene.text.Text;

import java.util.List;

/**
 * TextFlow.
 * @author Naotsugu Kobayashi
 */
public class TextFlow extends javafx.scene.text.TextFlow {

    /**
     * Constructor.
     */
    public TextFlow() {
        setPadding(new Insets(4));
        setTabSize(4);
    }

    void set(List<Text> texts) {
        getChildren().setAll(texts);
    }

}
