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
package com.mammb.code.editor3.model;

import com.mammb.code.editor3.model.specifics.ContentImpl;
import com.mammb.code.editor3.model.specifics.NowrapScrollHandler;
import com.mammb.code.editor3.model.specifics.WrapScrollHandler;
import javafx.scene.text.Text;
import java.nio.file.Path;
import java.util.List;

/**
 * TextView.
 * @author Naotsugu Kobayashi
 */
public class TextView {

    private final TextSlice textSlice;

    /** The caret point. */
    private final CaretPoint caretPoint = new CaretPoint();

    /** text wrap?. */
    private boolean textWrap;

    /** dirty?. */
    private boolean dirty;

    /** The scroll handler. */
    private ScrollHandler scrollHandler;

    /** The caret handler. */
    private CaretHandler caretHandler;


    /**
     * Constructor.
     * @param textSlice the text slice
     */
    public TextView(TextSlice textSlice) {
        this.textSlice = textSlice;
        initHandler();
    }


    /**
     * Create text view.
     * @param path the content path
     */
    public TextView(Path path) {
        this(new TextSlice(new TextSource(new ContentImpl(path))));
    }


    /**
     * Add text in caret point.
     * @param string the string to add
     */
    public void add(String string) {
        if (string == null || string.isEmpty()) return;
        textSlice.insert(caretPoint.offset(), string);
        caretPoint.forward(string);
    }


    /**
     * Add text in caret point.
     * @param length the length of delete string
     */
    public void delete(int length) {
        if (length <= 0) return;
        textSlice.delete(caretPoint.offset(), length);
        caretPoint.syncPositionOnRow();
    }


    /**
     * Set the max row number.
     * @param maxRows the max row number
     */
    public void setupMaxRows(int maxRows) {
        if (maxRows > 1 && textSlice.maxRowSize() != maxRows) {
            textSlice.setMaxRowSize(maxRows);
            textSlice.refresh();
        }
    }


    /**
     * Gets whether this text is dirty.
     * @return {@code true} if text is dirty.
     */
    public boolean isDirty() { return dirty; }


    public List<Text> text() {
        return List.of(new Text(textSlice.string()));
    }


    private void initHandler() {
        if (textWrap) {
            scrollHandler = new WrapScrollHandler();
        } else {
            scrollHandler = new NowrapScrollHandler(textSlice, caretPoint);
        }
    }

}
