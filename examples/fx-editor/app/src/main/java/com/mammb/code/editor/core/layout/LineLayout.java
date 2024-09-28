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

import com.mammb.code.editor.core.text.Text;

/**
 * The line layout.
 * @author Naotsugu Kobayashi
 */
public interface LineLayout {

    int rowToFirstLine(int row);
    int rowToLastLine(int row);
    int rowToLine(int row, int col);
    int lineToRow(int line);
    int lineSize();
    Text text(int line);
    Text rowTextAt(int row);
    double lineHeight();

    int homeColOnRow(int line);
    default int endColOnRow(int line) { return homeColOnRow(line) + text(line).textLength(); }

    int xToCol(int line, double x);
    default double xOnLayout(int line, int col) {
        return text(line).widthTo(col);
    }
    default double yOnLayout(int line) {
        return line * lineHeight();
    }

}
