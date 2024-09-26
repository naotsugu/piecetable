/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.text.RowText;
import com.mammb.code.editor.core.text.Text;
import java.util.List;
import java.util.Optional;

/**
 * The Layout.
 * @author Naotsugu Kobayashi
 */
interface Layout {

    int rowToFirstLine(int row);
    int rowToLastLine(int row);
    int rowToLine(int row, int col);
    int lineToRow(int line);
    int lineSize();
    int rowSize();

    void setWidth(double width);
    void refresh(int line);
    void refreshAt(int startRow, int endRow);
    Text text(int line);
    List<Text> texts(int startLine, int endLine);
    RowText rowText(int line);
    RowText rowTextAt(int row);
    double lineHeight();
    int xToCol(int line, double x);
    int homeColOnRow(int line);
    FontMetrics fontMetrics();

    /**
     *
     * @param row
     * @param col
     * @param startLine the limit
     * @param endLine the limit
     * @return
     */
    Optional<Loc> loc(int row, int col, int startLine, int endLine);

    default double x(int line, int col) {
        return text(line).widthTo(col);
    }

    default double y(int line) {
        return line * lineHeight();
    }

    default int yToLine(double y) {
        return (int) (y / lineHeight());
    }

    default int endColOnRow(int line) {
        return homeColOnRow(line) + text(line).textLength();
    }

}
