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

import com.mammb.code.editor3.ui.Pointing;
import com.mammb.code.editor3.ui.TextFlow;
import com.mammb.code.editor3.ui.util.Texts;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * ImeBehavior.
 * @author Naotsugu Kobayashi
 */
public class ImeBehavior {

    /** The text flow. */
    private final TextFlow flow;

    /** The pointing. */
    private final Pointing pointing;

    /** The edit behavior. */
    private final EditBehavior editBehavior;

    /** The pending text. */
    private Text pendingText = null;

    /** the x positions before ime conversion starts. */
    private Double originalTranslateX = null;


    /**
     * Constructor.
     * @param flow the text flow
     * @param pointing the pointing
     * @param editBehavior the edit behavior
     */
    public ImeBehavior(TextFlow flow, Pointing pointing, EditBehavior editBehavior) {
        this.flow = flow;
        this.pointing = pointing;
        this.editBehavior = editBehavior;
    }


    /**
     * Apply the committed text.
     * @param text the committed text
     */
    public void committed(String text) {
        exit();
        editBehavior.input(text);
    }


    /**
     * Apply the composed text in the process of conversion..
     * @param text the composed text
     */
    public void composed(String text) {
        if (text == null || text.isEmpty()) return;
        if (!doing()) {
            on(pointing.caretOffset());
        }
        pendingText.setText(text);
        pointing.slipCaretTranslateX(originalTranslateX + Texts.width(pendingText.getText(), pendingText.getFont()));
    }


    /**
     * Exit ime.
     */
    public void exit() {
        if (pendingText != null) {
            boolean rm = flow.getChildren().remove(pendingText);
            if (!rm) throw new RuntimeException("ime exit error.");
            pendingText = null;
        }
        if (originalTranslateX != null) {
            pointing.slipCaretTranslateX(originalTranslateX);
            originalTranslateX = null;
        }
    }


    public boolean doing() {
        return pendingText != null;
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

                    Text left = Texts.copy(text);
                    left.setText(string.substring(0, caretIndex - count));
                    texts.add(left);

                    pendingText = Texts.copy(text);
                    pendingText.setText("");
                    pendingText.setUnderline(true);
                    texts.add(pendingText);

                    Text right = Texts.copy(text);
                    right.setText(string.substring(caretIndex - count));
                    texts.add(right);

                } else if (pendingText == null && caretIndex == count + length) {
                    texts.add(text);
                    pendingText = Texts.copy(text);
                    pendingText.setText("");
                    pendingText.setUnderline(true);
                    texts.add(pendingText);
                } else {
                    texts.add(text);
                }
                count += length;
            }

        }
        flow.setAll(texts);
        originalTranslateX = pointing.caretTranslateX();

    }


    /**
     * Get the palette location.
     * @return the palette location
     */
    public Point2D paletteLocation() {
        return pointing.localToScreen(
            pointing.caretX() - pointing.getTranslateX(),
            pointing.caretBottom() - pointing.getTranslateY());
    }


    /**
     * Create input method request.
     * @return the InputMethodRequests
     */
    public InputMethodRequests inputMethodRequests() {
        return new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int offset) {
                return paletteLocation();
            }
            @Override
            public int getLocationOffset(int x, int y) {
                return 0;
            }
            @Override
            public void cancelLatestCommittedText() { exit(); }
            @Override
            public String getSelectedText() {
                return "";
            }
        };
    }

}
