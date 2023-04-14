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

import com.mammb.code.editor3.syntax.DecoratorImpl;
import com.mammb.code.editor3.lang.Paths;
import java.nio.file.Path;
import java.util.List;

/**
 * TextModel.
 * @author Naotsugu Kobayashi
 */
public class TextModel {

    /** The text slice. */
    private final TextSlice textSlice;

    /** The text decorator. */
    private Decorator decorator;

    /** dirty?. */
    private boolean dirty;


    /**
     * Constructor.
     * @param textSlice the text slice
     */
    public TextModel(TextSlice textSlice) {
        this.textSlice = textSlice;
        this.decorator = DecoratorImpl.of("");
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
     * Open file with specified path.
     * @param path the path of opening file
     */
    public void open(Path path) {
        if (dirty) {
            throw new IllegalStateException();
        }
        textSlice.open(path);
        decorator = DecoratorImpl.of(Paths.getExtension(path));
    }


    /**
     * Save.
     */
    public void save() {
        textSlice.save();
        dirty = false;
    }


    /**
     * Save as the specified path.
     * @param path the specified path
     */
    public void saveAs(Path path) {
        textSlice.saveAs(path);
        decorator = DecoratorImpl.of(Paths.getExtension(path));
        dirty = false;
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
     * Peek undo offset.
     * @return the position of undo
     */
    public OffsetPoint undoPeek() {
        return textSlice.undoPeek();
    }


    /**
     * Peek redo offset.
     * @return the position of redo
     */
    public OffsetPoint redoPeek() {
        return textSlice.redoPeek();
    }


    /**
     * Undo.
     */
    public void undo() {
        textSlice.undo();
    }


    /**
     * Redo.
     */
    public void redo() {
        textSlice.redo();
    }


    public int scrollNext(int rawDelta) {
        if (rawDelta <= 0) return 0;
        int old = textSlice.originOffset();
        textSlice.shiftRow(rawDelta);
        return Math.abs(textSlice.originOffset() - old);
    }


    public int scrollPrev(int delta) {
        if (delta <= 0) return 0;
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
     * The capacity of row on slice.
     * @return the capacity of row on slice
     */
    public int capacityOfRows() {
        return textSlice.maxRowSize();
    }


    /**
     * Gets whether this text is dirty.
     * @return {@code true} if text is dirty.
     */
    public boolean isDirty() { return dirty; }


    /**
     * Gets whether a subsequent slice exists.
     * @return {@code true} if exists next
     */
    public boolean hasNextSlice() {
        return textSlice.hasNext();
    }


    /**
     * Get the total row size.
     * @return the total row size
     */
    public int totalRowSize() {
        return textSlice.totalRowSize();
    }


    /**
     * Get the number of origin row index.
     * @return the number of origin row index
     */
    public int originRowIndex() {
        return textSlice.originRow();
    }


    /**
     * Get the number of tail row index.
     * @return the number of tail row index
     */
    public int tailRowIndex() {
        return textSlice.tailRow();
    }


    /**
     * Get the origin offset(not code point counts).
     * @return the origin offset(not code point counts)
     */
    public int originOffset() {
        return textSlice.originOffset();
    }


    /**
     * Get the tail offset(not code point counts).
     * @return the tail offset(not code point counts)
     */
    public int tailOffset() {
        return textSlice.tailOffset();
    }


    /**
     * Get the content path.
     * @return the content path. {@code null} if content path is empty
     */
    public Path contentPath() {
        return textSlice.contentPath();
    }


    /**
     * Get the sub string.
     * @param offset the char offset(not codepoint) on the slice
     * @param length the char length(not codepoint)
     * @return the string
     */
    public String substring(int offset, int length) {
        return textSlice.substring(offset, length);
    }


    /**
     * Gst the decorated text list.
     * @return the decorated text list
     */
    public List<DecoratedText> text() {
        return decorator.apply(textSlice.origin(), textSlice.string());
    }


    /**
     * Gst the string on slice.
     * @return the string on slice
     */
    public String stringSlice() {
        return textSlice.string();
    }

}
