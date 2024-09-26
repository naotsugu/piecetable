/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.core;

import com.mammb.code.editor.core.layout.Loc;
import com.mammb.code.editor.core.model.PlainEditorModel;
import com.mammb.code.editor.core.syntax.Syntax;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The Facade of editor.
 */
public interface EditorModel {

    static EditorModel of(FontMetrics fm, ScreenScroll scroll) {
        return new PlainEditorModel(
                Content.of(),
                fm,
                Syntax.of(""),
                scroll);
    }

    static EditorModel of(Path path, FontMetrics fm, ScreenScroll scroll) {
        return new PlainEditorModel(
                Content.of(path),
                fm,
                Syntax.of(extension(path)),
                scroll);
    }

    void draw(Draw draw);
    void setSize(double width, double height);

    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int line);
    void scrollX(double x);
    void scrollToCaret();
    void moveCaretRight(boolean withSelect);
    void moveCaretLeft(boolean withSelect);
    void moveCaretDown(boolean withSelect);
    void moveCaretUp(boolean withSelect);
    void moveCaretHome(boolean withSelect);
    void moveCaretEnd(boolean withSelect);
    void moveCaretPageUp(boolean withSelect);
    void moveCaretPageDown(boolean withSelect);
    void click(double x, double y, boolean withSelect);
    void clickDouble(double x, double y);
    void clickTriple(double x, double y);
    void moveDragged(double x, double y);

    void input(String text);
    void delete();
    void backspace();
    void undo();
    void redo();
    void pasteFromClipboard();
    void copyToClipboard();
    void cutToClipboard();
    boolean isModified();
    Optional<Path> path();
    void save(Path path);
    void escape();

    Optional<Loc> imeOn();
    void imeOff();
    boolean isImeOn();
    void inputImeComposed(String text);

    private static String extension(Path path) {
        return Optional.of(path.getFileName().toString())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1))
                .orElse("");
    }

}
