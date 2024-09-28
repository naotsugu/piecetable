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
 * The content layout.
 * @author Naotsugu Kobayashi
 */
interface ContentLayout extends LineLayout {

    void setScreenWidth(double width);
    void refresh(int line);
    void refreshAt(int startRow, int endRow);
    List<Text> texts(int startLine, int endLine);
    RowText rowText(int line);
    @Override
    RowText rowTextAt(int row);
    FontMetrics fontMetrics();
    Optional<Loc> loc(int row, int col, int rangeLineStart, int rangeLineEnd);

}
