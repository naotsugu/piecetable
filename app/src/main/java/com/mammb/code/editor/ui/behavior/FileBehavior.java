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
package com.mammb.code.editor.ui.behavior;

import com.mammb.code.editor.ui.TextPane;
import com.mammb.code.editor.ui.util.FileChoosers;
import java.io.File;
import java.util.Objects;

/**
 * FileBehavior.
 * @author Naotsugu Kobayashi
 */
public class FileBehavior {

    /** The text pane. */
    private final TextPane textPane;


    /**
     * Constructor.
     * @param textPane the text pane
     */
    public FileBehavior(TextPane textPane) {
        this.textPane = Objects.requireNonNull(textPane);
    }


    /**
     * Open file.
     */
    public void open() {
        textPane.open(() -> {
            File file = FileChoosers.fileChooseOpen(textPane.stage(), null);
            if (file != null) {
                textPane.open(file.toPath());
            }
        });
    }


    /**
     * Save
     */
    public void save() {
        if (textPane.contentPath() == null) {
            File file = FileChoosers.fileChooseSave(textPane.stage(), null);
            if (file != null) {
                textPane.saveAs(file.toPath());
            }
        } else {
            textPane.save();
        }
    }


    /**
     * Save as
     */
    public void saveAs() {
        File file = FileChoosers.fileChooseSave(textPane.stage(), null);
        if (file != null) {
            textPane.saveAs(file.toPath());
        }
    }

}
