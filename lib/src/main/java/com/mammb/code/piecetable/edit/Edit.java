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

import com.mammb.code.piecetable.TextEdit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The edit.
 * @author Naotsugu Kobayashi
 */
sealed interface Edit {

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
            case Ins e -> new Del(e.range, e.text, e.occurredOn);
            case Del e -> new Ins(e.range, e.text, e.occurredOn);
            case Cmp e -> {
                List<Edit> flipped = e.edits.stream().map(Edit::flip).collect(Collectors.toList());
                Collections.reverse(flipped);
                yield new Cmp(flipped, e.occurredOn);
            }
        };
    }


    default Optional<Edit> merge(Edit that) {
        return switch (this) {
            case Ins e when that instanceof Ins t &&
                nearly(e, t) && e.range.left() &&
                e.range.col() + e.text.length() == t.range.col() ->
                Optional.of(new Ins(e.range, e.text + t.text, t.occurredOn));
            case Ins e when that instanceof Ins t &&
                nearly(e, t) && e.range.right() &&
                e.range.col() - e.text.length() == t.range.col() ->
                Optional.of(new Ins(e.range, t.text + e.text, t.occurredOn));
            case Del e when that instanceof Del t &&
                nearly(e, t) && e.range.left() &&
                e.range.col() - e.text.length() == t.range.col() ->
                Optional.of(new Del(e.range, t.text + e.text, t.occurredOn));
            case Del e when that instanceof Del t &&
                nearly(e, t) && e.range.right() &&
                e.range.col() == t.range.col() ->
                Optional.of(new Del(e.range, e.text + t.text, t.occurredOn));
            default -> Optional.empty();
        };
    }


    private static boolean nearly(ConcreteEdit e1, ConcreteEdit e2) {
        return e1.occurredOn() - e2.occurredOn() < 2500 &&
            !e1.text().contains("\n") && !e2.text().contains("\n") &&
            e1.range().left() == e2.range().left() &&
            e1.range().row() == e2.range().row();
    }


    sealed interface ConcreteEdit extends Edit {
        TextEdit.Range range();
        String text();
    }

    record Cmp(List<Edit> edits, long occurredOn) implements Edit {}
    record Ins(TextEdit.Range range, String text, long occurredOn) implements ConcreteEdit {}
    record Del(TextEdit.Range range, String text, long occurredOn) implements ConcreteEdit {}

    static Edit insert(int row, int col, String text) {
        return new Ins(TextEdit.Range.leftOf(row, col), text, System.currentTimeMillis());
    }

    static Edit delete(int row, int col, String text) {
        return new Del(TextEdit.Range.rightOf(row, col), text, System.currentTimeMillis());
    }

    static Edit backspace(int row, int col, String text) {
        return new Del(TextEdit.Range.leftOf(row, col), text, System.currentTimeMillis());
    }

    static Edit replace(int fromRow, int fromCol, int toRow, int toCol, String beforeText, String afterText) {
        long occurredOn = System.currentTimeMillis();
        boolean forward = (fromRow < toRow || (fromRow == toRow && fromCol < toCol));
        if (forward) {
            //  |0|1|2|3|4|5|
            //    ^     ^
            //    |     |
            //    |     caret(to)
            //    select start(from)
            return new Cmp(List.of(
                new Del(TextEdit.Range.leftOf(toRow, toCol), beforeText, occurredOn),
                new Ins(TextEdit.Range.leftOf(fromRow, fromCol), afterText, occurredOn)), occurredOn);
        } else {
            //  |0|1|2|3|4|5|
            //    ^     ^
            //    |     |
            //    |     select start(from)
            //    caret(to)
            return new Cmp(List.of(
                new Del(TextEdit.Range.rightOf(fromRow, fromCol), beforeText, occurredOn),
                new Del(TextEdit.Range.rightOf(fromRow, fromCol), beforeText, occurredOn)), occurredOn);
        }
    }

}
