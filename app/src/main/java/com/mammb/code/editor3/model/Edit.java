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

    public static Edit empty = new Edit(Type.NIL, 0, "", 0);

    private enum Type { INSERT, DELETE, NIL }

    private final Type type;
    private final int position;
    private final String string;
    private final long occurredOn;
    private int codePointCount;

    public Edit(Type type, int position, String string, long occurredOn) {
        this.type = type;
        this.position = position;
        this.string = string;
        this.occurredOn = occurredOn;
    }

    public static Edit insert(int position, String string) {
        return new Edit(Type.INSERT, position, string, System.currentTimeMillis());
    }

    public static Edit delete(int position, String string) {
        return new Edit(Type.DELETE, position, string, System.currentTimeMillis());
    }

    public boolean isInsert() { return type == Type.INSERT; }

    public boolean isDelete() { return type == Type.DELETE; }

    public boolean isEmpty() { return type == Type.NIL; }

    public Edit withOffset(int offset) {
        return new Edit(type, offset + position, string, occurredOn);
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
        return new Edit(this.type, this.position, this.string + other.string, other.occurredOn);
    }

    public int position() {
        return position;
    }

    public String string() {
        return string;
    }

    public int codePointCount() {
        if (codePointCount < 0) {
            codePointCount = Strings.codePointCount(string);
        }
        return codePointCount;
    }

}
