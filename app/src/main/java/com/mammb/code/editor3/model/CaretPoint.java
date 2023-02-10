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
 * CaretPoint.
 * @author Naotsugu Kobayashi
 */
public class CaretPoint {

    /** The offset. */
    private int offset;

    /** The number of row. */
    private int rowNumber;

    /** The position on row. */
    private int actualPositionOnRow;

    /** The prefer-position on row. */
    private int preferPositionOnRow;


    public void forward(String string) {
        offset += string.length();
        int lfCount = Strings.countLf(string);
        if (lfCount == 0) {
            actualPositionOnRow += Strings.lengthOfLastRow(string);
        } else {
            rowNumber += lfCount;
            actualPositionOnRow = Strings.lengthOfLastRow(string);
        }
        syncPositionOnRow();
    }

    public void syncPositionOnRow() {
        preferPositionOnRow = actualPositionOnRow;
    }


    public int offset() {
        return offset;
    }

    public int rowNumber() {
        return rowNumber;
    }

    public int actualPositionOnRow() {
        return actualPositionOnRow;
    }

    public int preferPositionOnRow() {
        return preferPositionOnRow;
    }
}
