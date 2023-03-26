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

import com.mammb.code.editor3.ui.behavior.EditBehavior;
import com.mammb.code.editor3.ui.util.Texts;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ime palette.
 * @author Naotsugu Kobayashi
 */
public class ImePalette {

    private final BooleanProperty imeOn = new SimpleBooleanProperty(false);

    private final TextFlow flow;

    private final Caret caret;

    private EditBehavior editBehavior;

    /** The pending text. */
    private Text pendingText;

    private Double caretXOriginal;


    /**
     * Constructor.
     * @param flow
     * @param caret
     */
    public ImePalette(TextFlow flow, Caret caret) {
        this.flow = flow;
        this.caret = caret;
    }

    public void bindBehavior(EditBehavior editBehavior) {
        this.editBehavior = editBehavior;
    }


    private void on(int caretIndex) {

        pendingText = null;
        int count = 0;
        List<Text> texts = new ArrayList<>();

        for (Node node : flow.getChildren()) {
            if (node instanceof Text text) {
                int length = (text.getText() == null) ? 0 :text.getText().length();
                if (pendingText == null && count < caretIndex && caretIndex < count + length) {
                    String string = text.getText();

                    Text left = copy(text);
                    left.setText(string.substring(0, caretIndex - count));
                    texts.add(left);

                    pendingText = copy(text);
                    pendingText.setText("");
                    texts.add(pendingText);

                    Text right = copy(text);
                    right.setText(string.substring(caretIndex - count));
                    texts.add(right);

                } else if (pendingText == null && caretIndex == count + length) {
                    pendingText = copy(text);
                    pendingText.setText("");
                    texts.add(pendingText);
                } else {
                    texts.add(text);
                }
                count += length;
            }

        }
        flow.setAll(texts);
        caretXOriginal = caret.getTranslateX();
    }


    public void handleInputMethod(InputMethodEvent e) {

        if (!imeOn.get()) {
            on(caret.offset());
            imeOn.set(true);
        }

        if (e.getCommitted().length() > 0) {
            clear();
            editBehavior.input(e.getCommitted());
        } else if (!e.getComposed().isEmpty()) {
            pendingText.setText(e.getComposed().stream()
                .map(InputMethodTextRun::getText).collect(Collectors.joining()));
            caret.setTranslateX(caretXOriginal + Texts.width(pendingText.getText(), pendingText.getFont()));
        }

        if (e.getCommitted().isEmpty() && e.getComposed().isEmpty()) {
            clear();
        }

    }

    private void clear() {
        if (pendingText != null) {
            flow.getChildren().remove(pendingText);
            pendingText = null;
        }
        if (caretXOriginal != null) {
            caret.setTranslateX(caretXOriginal);
            caretXOriginal = null;
        }
        imeOn.set(false);
    }


    public InputMethodRequests createInputMethodRequests() {
        return new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int offset) {
                Bounds bounds = flow.localToScreen(flow.getBoundsInLocal());
                return new Point2D(
                    bounds.getMinX() + caret.physicalX(),
                    bounds.getMinY() + caret.physicalYInParent() + caret.height());
            }
            @Override
            public int getLocationOffset(int x, int y) {
                return 0;
            }
            @Override
            public void cancelLatestCommittedText() { clear();}
            @Override
            public String getSelectedText() {
                return "";
            }
        };
    }


    public final boolean getImeOn() { return imeOn.get(); }
    void setImeOn(boolean value) { imeOn.set(value); }
    public BooleanProperty imeOnProperty() { return imeOn; }


    /**
     * Create the copy of text.
     * @param source the source
     * @return the copy
     */
    private static Text copy(Text source) {
        Text copy = new Text(source.getText());
        copy.setFont(source.getFont());
        copy.setFill(source.getFill());
        return copy;
    }

}
