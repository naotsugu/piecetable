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
import java.nio.file.Path;
import java.util.List;

/**
 * TextModel.
 * @author Naotsugu Kobayashi
 */
public class TextModel {

    /** The text slice. */
    private final TextSlice textSlice;

    /** text wrap?. */
    private boolean textWrap;

    /** dirty?. */
    private boolean dirty;


    /**
     * Constructor.
     * @param textSlice the text slice
     */
    public TextModel(TextSlice textSlice) {
        this.textSlice = textSlice;
    }


    /**
     * Create text view.
     * @param path the content path
     */
    public TextModel(Path path) {
        this(new TextSlice(new TextSource(new ContentImpl(path))));
    }


    /**
     * Create text view.
     */
    public TextModel() {
        this(new TextSlice(new TextSource(new ContentImpl())));
    }


    /**
     * Add text.
     * @param offset the offset to add
     * @param string the string to add
     */
    public void add(int offset, String string) {
        if (string == null || string.isEmpty()) return;
        textSlice.insert(offset, string);
    }


    /**
     * Delete text.
     * @param offset the offset to delete
     * @param length the length of delete string
     */
    public void delete(int offset, int length) {
        if (length <= 0) return;
        textSlice.delete(offset, length);
    }


    public int scrollNext(int delta) {
        int old = textSlice.originOffset();
        textSlice.shiftRow(delta);
        return Math.abs(textSlice.originOffset() - old);
    }


    public int scrollPrev(int delta) {
        if (textSlice.originRow() == 0) return 0;
        int old = textSlice.originOffset();
        textSlice.shiftRow(-delta);
        return Math.abs(textSlice.originOffset() - old);
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


    /**
     * Get the number of origin row index.
     * @return the number of origin row index
     */
    public int originRowIndex() {
        return textSlice.originRow();
    }



    public List<String> text() {
        // TODO token string
        return List.of(textSlice.string());
    }


}
