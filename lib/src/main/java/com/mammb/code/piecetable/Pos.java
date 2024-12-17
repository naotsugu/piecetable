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
package com.mammb.code.piecetable;

/**
 * The position record.
 *
 * @param row the number of row(zero origin)
 * @param col the byte position on the row
 * @author Naotsugu Kobayashi
 */
public record Pos(int row, int col) implements Comparable<Pos> {
    @Override
    public int compareTo(Pos that) {
        int c = Integer.compare(this.row, that.row);
        return c == 0 ? Integer.compare(this.col, that.col) : c;
    }
}
