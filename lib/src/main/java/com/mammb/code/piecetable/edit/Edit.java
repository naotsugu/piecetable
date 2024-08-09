/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.piecetable.edit;

/**
 * The edit.
 * @author Naotsugu Kobayashi
 */
public sealed interface Edit {

    /**
     * Get the number of row(zero origin).
     * @return the number of row(zero origin)
     */
    int row();

    /**
     * Get the byte position on the row where the char sequence is to be edited.
     * @return the byte position on the row
     */
    int col();

    /**
     * Get the occurrence time of this edit.
     * @return the occurrence time
     */
    long occurredOn();

    /**
     * Flip this edit.
     * @return the flipped edit.
     */
    default Edit flip() {
        return switch (this) {
            case Insert e -> new Delete(e.row(), e.col(), e.text(), e.occurredOn());
            case Delete e -> new Insert(e.row(), e.col(), e.text(), e.occurredOn());
            case BsInsert e -> new BsDelete(e.row(), e.col(), e.text(), e.occurredOn());
            case BsDelete e -> new BsInsert(e.row(), e.col(), e.text(), e.occurredOn());
            default -> empty;
        };
    }

    /** The empty edit. */
    Edit empty = new Empty();

    interface Txt { String text(); }
    sealed interface Ins extends Edit, Txt { }
    sealed interface Del extends Edit, Txt { }

    record Insert(int row, int col, String text, long occurredOn) implements Edit, Ins {}
    record Delete(int row, int col, String text, long occurredOn) implements Edit, Del {}
    record BsInsert(int row, int col, String text, long occurredOn) implements Edit, Ins {}
    record BsDelete(int row, int col, String text, long occurredOn) implements Edit, Del {}
    record Empty() implements Edit {
        @Override public int row() { return 0; }
        @Override public int col() { return 0; }
        @Override public long occurredOn() { return 0; }
    }

}
