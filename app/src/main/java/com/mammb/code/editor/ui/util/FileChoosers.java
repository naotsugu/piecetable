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
package com.mammb.code.editor.ui.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.nio.file.Path;

/**
 * The fileChooser utility.
 * @author Naotsugu Kobayashi
 */
public class FileChoosers {

    /**
     * Choose open file.
     * @param owner the owner window
     * @param path the base path
     * @return the path
     */
    public static File fileChooseOpen(Window owner, Path path) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        if (path == null) {
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
        } else if (path.toFile().isDirectory()) {
            fc.setInitialDirectory(path.toFile());
        } else {
            fc.setInitialDirectory(path.getParent().toFile());
        }
        return fc.showOpenDialog(owner);
    }


    /**
     * Choose save file.
     * @param owner the owner window
     * @param path the base path
     * @return the path
     */
    public static File fileChooseSave(Window owner, Path path) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save As...");
        if (path == null) {
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
        } else if (path.toFile().isDirectory()) {
            fc.setInitialDirectory(path.toFile());
        } else {
            fc.setInitialDirectory(path.getParent().toFile());
        }
        return fc.showSaveDialog(owner);
    }

}
