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

import com.mammb.code.editor3.model.behavior.CaretBehavior;
import com.mammb.code.editor3.model.behavior.ScrollBehavior;
import com.mammb.code.editor3.model.specifics.ContentImpl;
import com.mammb.code.editor3.model.behavior.NowrapScrollBehavior;
import com.mammb.code.editor3.model.behavior.WrapScrollBehavior;
import java.nio.file.Path;
import java.util.List;

/**
 * TextView.
 * @author Naotsugu Kobayashi
 */
public class TextView {

    /** The text slice. */
    private final TextSlice textSlice;

    /** text wrap?. */
    private boolean textWrap;

    /** dirty?. */
    private boolean dirty;

    /** The scroll behavior. */
    private ScrollBehavior scrollBehavior;

    /** The caret behavior. */
    private CaretBehavior caretBehavior;


    /**
     * Constructor.
     * @param textSlice the text slice
     */
    public TextView(TextSlice textSlice) {
        this.textSlice = textSlice;
        initBehavior();
    }


    /**
     * Create text view.
     * @param path the content path
     */
    public TextView(Path path) {
        this(new TextSlice(new TextSource(new ContentImpl(path))));
    }


    /**
     * Create text view.
     */
    public TextView() {
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
     * Get the {@link ScrollBehavior}.
     * @return the {@link ScrollBehavior}
     */
    public ScrollBehavior scrollBehavior() { return scrollBehavior; }



    public List<String> text() {
        return List.of(textSlice.string());
    }


    private void initBehavior() {
        if (textWrap) {
            scrollBehavior = new WrapScrollBehavior();
        } else {
            scrollBehavior = new NowrapScrollBehavior(textSlice);
        }
    }


}
