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
 * ScopeType.
 * @author Naotsugu Kobayashi
 */
public enum ScopeType {

    NEUTRAL,
    BLOCK_START,
    BLOCK_ANY,
    BLOCK_END,
    INLINE_START,
    INLINE_ANY,
    INLINE_END,
    ;

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public boolean isBlock() {
        return this == BLOCK_START || this == BLOCK_ANY || this == BLOCK_END;
    }

    public boolean isInline() {
        return this == INLINE_START || this == INLINE_ANY || this == INLINE_END;
    }

    public boolean isStart() {
        return this == BLOCK_START || this == INLINE_START;
    }

    public boolean isEnd() {
        return this == BLOCK_END || this == INLINE_END;
    }

    public boolean isAny() {
        return this == BLOCK_ANY || this == INLINE_ANY;
    }

}

