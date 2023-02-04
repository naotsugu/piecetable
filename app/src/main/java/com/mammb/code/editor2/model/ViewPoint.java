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
package com.mammb.code.editor2.model;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ViewPoint.
 * @author Naotsugu Kobayashi
 */
public class ViewPoint implements EventListener<Edit> {

    /** The text content. */
    private Content content;

    /** The code point index of start on view. */
    private int contentIndex;

    /** The target view. */
    private TextView textView;


    /**
     * Constructor.
     * @param content the text content
     */
    public ViewPoint(Content content) {
        this.content = content;
        this.contentIndex = 0;
        this.textView = new TextView(this);
    }

    @Override
    public void handle(Edit edit) {
        content.handle(
            edit.posWith(contentIndex + textView.codePointCount(0, edit.pos())));
    }

    /**
     * Get the text view.
     * @return the text view
     */
    public TextView textView() {
        return textView;
    }

    /**
     * Get the code point length of view.
     */
    private int length() {
        return textView.codePointCount();
    }

    String scrollNext(int deltaCodePoint) {
        byte[] tailRow = content.bytes(contentIndex + textView.codePointCount(), b -> b[0] == '\n');
        contentIndex += deltaCodePoint;
        return new String(tailRow, StandardCharsets.UTF_8);
    }

    String scrollPrev(int deltaCodePoint) {
        AtomicInteger count = new AtomicInteger(2);
        byte[] headRow = content.bytesBefore(contentIndex, b -> count.addAndGet(b[0] == '\n' ? -1 : 0) == 0);
        contentIndex -= deltaCodePoint;
        return new String(headRow, StandardCharsets.UTF_8);
    }

}
