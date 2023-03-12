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

import com.mammb.code.editor3.lang.Strings;

/**
 * Edit.
 * @author Naotsugu Kobayashi
 */
public class Edit {

    /** The empty edit. */
    public static Edit empty = new Edit(Type.NIL, 0, "", 0);

    /** The type of edit. */
    private enum Type { INSERT, DELETE, NIL }

    /** The type of edit. */
    private final Type type;
    /** The position of edit. */
    private final int position;
    /** The edited string. */
    private final String string;
    /** The occurred on. */
    private final long occurredOn;
    /** The code point count of edited string. */
    private int codePointCount;

    /**
     * Constructor.
     * @param type the type of edit
     * @param position the position of edit
     * @param string the edited string
     * @param occurredOn the occurred on
     */
    private Edit(Type type, int position, String string, long occurredOn) {
        this.type = type;
        this.position = position;
        this.string = string;
        this.occurredOn = occurredOn;
    }

    /**
     * Create the insert edit.
     * @param position the position of edit
     * @param string the edited string
     * @return
     */
    public static Edit insert(int position, String string) {
        return new Edit(Type.INSERT, position, string, System.currentTimeMillis());
    }

    /**
     * Create the deleted edit.
     * @param position the position of edit
     * @param string the edited string
     * @return
     */
    public static Edit delete(int position, String string) {
        return new Edit(Type.DELETE, position, string, System.currentTimeMillis());
    }

    public boolean isInsert() { return type == Type.INSERT; }

    public boolean isDelete() { return type == Type.DELETE; }

    public boolean isEmpty() { return type == Type.NIL; }

    public Edit withPosition(int newPosition) {
        return new Edit(type, newPosition, string, occurredOn);
    }

    public boolean canMarge(Edit other) {
        if (this.type == Type.NIL) {
            return true;
        }
        return this.type == other.type &&
            other.occurredOn - this.occurredOn < 2000 &&
            this.position + codePointCount() == other.position;
    }

    public Edit marge(Edit other) {
        return new Edit(other.type, other.position, this.string + other.string, other.occurredOn);
    }

    /**
     * Get the position of edit.
     * @return the position of edit
     */
    public int position() {
        return position;
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

}
