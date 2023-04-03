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
package com.mammb.code.editor3.syntax;

/**
 * TokenType.
 * @author Naotsugu Kobayashi
 */
public enum TokenType {
    ANY,
    EMPTY,
    SP,
    KEYWORD,
    NUMBER,
    LITERAL,
    TEXT,
    LINE_COMMENT,
    COMMENT,
    ATTR,
    EOL,
    H1,
    H2,
    H3,
    H4,
    H5,
    ;
}
