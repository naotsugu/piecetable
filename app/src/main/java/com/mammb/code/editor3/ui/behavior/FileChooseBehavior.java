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
package com.mammb.code.editor3.ui.behavior;

import com.mammb.code.editor3.ui.TextPane;
import java.io.File;

import static com.mammb.code.editor3.ui.util.FileChoosers.fileChooseOpen;

/**
 * FileChooseBehavior.
 * @author Naotsugu Kobayashi
 */
public class FileChooseBehavior {

    /** The text pane. */
    private final TextPane textPane;


    /**
     * Constructor.
     * @param textPane the text pane
     */
    public FileChooseBehavior(TextPane textPane) {
        this.textPane = textPane;
    }


    public void open() {
        File file = fileChooseOpen(textPane.stage(), null);
        if (file != null) {
            textPane.open(file.toPath());
        }
    }
}
