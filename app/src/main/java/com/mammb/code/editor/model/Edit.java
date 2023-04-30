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
package com.mammb.code.editor.model;

import com.mammb.code.editor.lang.Strings;

import java.util.StringJoiner;

/**
 * Edit.
 * @author Naotsugu Kobayashi
 */
public class Edit {

    /** The empty edit. */
    public static Edit empty = new Edit(EditType.NIL, OffsetPoint.empty, 0, "", 0);

    /** The type of edit. */
    private final EditType type;

    /** The offset point of edit. */
    private final OffsetPoint offset;

    /** The code point position. */
    private final int codePointPosition;

    /** The edited string. */
    private final String string;

    /** The occurred on. */
    private final long occurredOn;

    /** The code point count of edited string. */
    private int codePointCount = -1;


    /**
     * Constructor.
     * @param type the type of edit
     * @param offset the offset point of edit
     * @param codePointPosition the code point position of edit
     * @param string the edited string
     * @param occurredOn the occurred on
     */
    private Edit(EditType type, OffsetPoint offset, int codePointPosition, String string, long occurredOn) {
        this.type = type;
        this.offset = offset;
        this.codePointPosition = codePointPosition;
        this.string = string;
        this.occurredOn = occurredOn;
    }


    /**
     * Create the insert edit.
     * @param originOffset the origin offset of edit
     * @param position the position of edit
     * @param string the edited string
     * @return the Edit
     */
    public static Edit insert(int originOffset, int position, String string) {
        return new Edit(EditType.INSERT, new OffsetPoint(originOffset, position), -1, string, System.currentTimeMillis());
    }


    /**
     * Create the deleted edit.
     * @param originOffset the origin offset of edit
     * @param position the position of edit
     * @param string the edited string
     * @return the Edit
     */
    public static Edit delete(int originOffset, int position, String string) {
        return new Edit(EditType.DELETE, new OffsetPoint(originOffset, position), -1, string, System.currentTimeMillis());
    }


    /**
     * Get whether this edit is insert type.
     * @return {@code true}, if this edit is insert type
     */
    public boolean isInsert() { return type.isInsert(); }


    /**
     * Get whether this edit is delete type.
     * @return {@code true}, if this edit is delete type
     */
    public boolean isDelete() { return type.isDelete(); }


    /**
     * Get whether this edit is nil type.
     * @return {@code true}, if this edit is nil type
     */
    public boolean isEmpty() { return type.isNil(); }


    /**
     * Get the edit reflecting the code point.
     * @param codePointPosition the code point to be reflected.
     * @return the edit reflecting code point
     */
    public Edit withCodePointPosition(int codePointPosition) {
        return new Edit(type, offset, codePointPosition, string, occurredOn);
    }


    /**
     * Get whether other edit can be merged into this edit.
     * @param other the merging edit
     * @return {@code true} if other edit can be merged into this edit.
     */
    public boolean canMerge(Edit other) {
        if (this.type.isNil()) {
            return true;
        }
        if (this.type != other.type || other.occurredOn - this.occurredOn > 2000) {
            return false;
        }
        return isInsert()
            ? offset.position() + string.length() == other.offset.position()
            : offset.position() == other.offset.position();
    }


    /**
     * Merge other edit into this edit.
     * @param other the merging edit
     * @return the merged edit
     */
    public Edit merge(Edit other) {
        return isEmpty() ? other
            : new Edit(other.type,
                this.offset,
                this.codePointPosition,
                this.string + other.string,
                other.occurredOn);
    }


    /**
     * Flip this edit.
     * @return the flipped edit.
     */
    public Edit flip() {
        return new Edit(type.flip(), offset, codePointPosition, string, occurredOn);
    }


    /**
     * Get the offset point of edit.
     * @return the offset point of edit
     */
    public OffsetPoint offsetPoint() {
        return offset;
    }


    /**
     * Get the code point position of edit.
     * @return the code point position of edit
     */
    public int codePointPosition() {
        return codePointPosition;
    }


    /**
     * Get the edited string.
     * @return the edited string
     */
    public String string() {
        return string;
    }


    /**
     * Get the code point count of edited string.
     * @return the code point count of edited string
     */
    public int codePointCount() {
        if (codePointCount < 0) {
            codePointCount = Strings.codePointCount(string);
        }
        return codePointCount;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Edit.class.getSimpleName() + "[", "]")
            .add("type=" + type)
            .add("offset=" + offset)
            .add("codePointPosition=" + codePointPosition)
            .add("string='" + string + "'")
            .add("occurredOn=" + occurredOn)
            .add("codePointCount=" + codePointCount)
            .toString();
    }

}
