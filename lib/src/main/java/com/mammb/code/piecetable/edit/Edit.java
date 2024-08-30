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

import com.mammb.code.piecetable.TextEdit.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The edit.
 *
 * <pre>
 *  delete
 * ==============================================
 *  Del(from:1, to:1, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *        |               ^
 *       caret(from)
 *       to
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | f |   |   |   |   |
 *        |
 *       caret(to)
 *
 * ----------------------------------------------
 *  Del(from:1, to:1, len:4) -- flip --> Ins(from:1, to:1, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | d | f |   |   |   |
 *        |               ^
 *       caret(from)
 *       to
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *        |               ^
 *       caret(to)
 * </pre>
 *
 * <pre>
 *  backspace
 * ==============================================
 *  Del(from:5, to:1, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *        ^               |
 *        |               caret(from)
 *       to
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | d | f |   |   |   |
 *        |
 *       caret(to)
 *
 * ----------------------------------------------
 *  Del(from:5, to:1, len:4) -- flip --> Ins(from:1, to:5, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | d | f |   |   |   |
 *        |               ^
 *        caret(from)     |
 *                       to
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *                        |
 *                        caret(to)
 * </pre>
 *
 * <pre>
 *  insert
 * ==============================================
 *  Ins(from:1, to:5, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | f |   |   |   |   |
 *        |               ^
 *       caret(from)
 *
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *                        |
 *                        caret(to)
 *
 * ----------------------------------------------
 *  Ins(from:1, to:5, len:4) -- flip --> Del(from:5, to:1, len:4)
 *
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *        ^               |
 *        |              caret(from)
 *        to
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | f |   |   |   |   |
 *        |
 *       caret(to)
 * </pre>
 *
 * <pre>
 *  replace
 * ==============================================
 *  Del(from:5, to:1, len:4) [bcde]
 *  Ins(from:1, to:3, len:2) [xx]
 *
 *      0   1   2   3   4   5
 *         **************** selected
 *    | a | b | c | d | e | f |
 *            ^           |
 *            |          caret(from)
 *            to
 *
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | x | x | f |   |   |
 *                |
 *               caret(to)
 *
 * ----------------------------------------------
 *  -- flip --> Del(from:3, to:1, len:2) [xx]
 *              Ins(from:1, to:5, len:4) [bcde]
 *
 *      0   1   2   3   4   5
 *    | a | x | x | f |   |   |
 *                |
 *               caret(from)
 *
 * ----------------------------------------------
 *      0   1   2   3   4   5
 *    | a | b | c | d | e | f |
 *                        |
 *                       caret
 * </pre>
 *
 * @author Naotsugu Kobayashi
 */
sealed interface Edit {

    /**
     * Get the occurrence time of this edit.
     * @return the occurrence time
     */
    long occurredOn();

    /**
     * Flip the edit.
     * @return the flipped edit
     */
    default Edit flip() {
        return switch (this) {
            case Ins e -> new Del(e.to, e.from, e.text, e.occurredOn);
            case Del e -> new Ins(e.to, e.from, e.text, e.occurredOn);
            case Cmp e -> {
                List<ConcreteEdit> flipped = e.edits.stream()
                    .map(Edit::flip).map(ConcreteEdit.class::cast).collect(Collectors.toList());
                Collections.reverse(flipped);
                yield new Cmp(flipped, e.occurredOn);
            }
        };
    }

    /**
     * Merge the edit.
     * @param that the edit
     * @return Merged edit. {@code Optional.empty()} if can not merge
     */
    default Optional<Edit> merge(Edit that) {
        return switch (this) {
            case Ins e when that instanceof Ins t && nearly(e, t) -> {
                String text = e.forward() ? e.text + t.text : t.text + e.text;
                yield Optional.of(new Ins(e.from, t.to, text, t.occurredOn));
            }
            case Del e when that instanceof Del t && nearly(e, t) -> {
                String text = e.backward() ? t.text + e.text : e.text + t.text;
                yield Optional.of(new Del(e.from, t.to, text, t.occurredOn));
            }
            default -> Optional.empty();
        };
    }

    private static boolean nearly(ConcreteEdit e1, ConcreteEdit e2) {
        return e1.occurredOn() - e2.occurredOn() < 2500 &&
            !e1.text().contains("\n") && !e2.text().contains("\n") &&
            e1.from().row() == e2.from().row() &&
            e1.to().col() == e2.from().col() &&
            e1.forward() == e2.forward();
    }

    sealed interface ConcreteEdit extends Edit {
        Pos from(); Pos to(); String text();
        default boolean forward() {
            return (from().row() < to().row() || (from().row() == to().row() && from().col() < to().col()));
        }
        default boolean backward() {
            return (from().row() > to().row() || (from().row() == to().row() && from().col() > to().col()));
        }
        default Pos min() { return backward() ? to() : from(); }
    }
    record Cmp(List<? extends ConcreteEdit> edits, long occurredOn) implements Edit { }
    record Ins(Pos from, Pos to, String text, long occurredOn) implements ConcreteEdit {
        public Ins(Pos from, Pos to, String text) { this(from, to, text, System.currentTimeMillis()); }
    }
    record Del(Pos from, Pos to, String text, long occurredOn) implements ConcreteEdit {
        public Del(Pos from, Pos to, String text) { this(from, to, text, System.currentTimeMillis()); }
        public Del(Pos pos, String text, long occurredOn) { this(pos, pos, text, System.currentTimeMillis()); }

    }

}
