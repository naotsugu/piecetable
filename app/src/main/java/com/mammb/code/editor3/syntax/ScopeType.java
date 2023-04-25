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

    /** The type of neutral. */
    NEUTRAL,
    /** The type of block start. */
    BLOCK_START,
    /** The type of block any. */
    BLOCK_ANY,
    /** The type of block end. */
    BLOCK_END,
    /** The type of inline start. */
    INLINE_START,
    /** The type of inline any. */
    INLINE_ANY,
    /** The type of inline end. */
    INLINE_END,
    /** The type of context start. */
    CONTEXT_START,
    /** The type of context any. */
    CONTEXT_ANY,
    /** The type of context end. */
    CONTEXT_END,
    ;

    /**
     * Get whether this type is the type of neutral.
     * @return {@code true}, if this type is the type of neutral
     */
    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    /**
     * Get whether this type is the type of block.
     * @return {@code true}, if this type is the type of block
     */
    public boolean isBlock() {
        return this == BLOCK_START || this == BLOCK_ANY || this == BLOCK_END;
    }

    /**
     * Get whether this type is the type of inline.
     * @return {@code true}, if this type is the type of inline
     */
    public boolean isInline() {
        return this == INLINE_START || this == INLINE_ANY || this == INLINE_END;
    }

    /**
     * Get whether this type is the type of context.
     * @return {@code true}, if this type is the type of context
     */
    public boolean isContext() {
        return this == CONTEXT_START || this == CONTEXT_ANY || this == CONTEXT_END;
    }

    /**
     * Get whether this type is the type of start.
     * @return {@code true}, if this type is the type of start
     */
    public boolean isStart() {
        return this == BLOCK_START || this == INLINE_START || this == CONTEXT_START;
    }

    /**
     * Get whether this type is the type of end.
     * @return {@code true}, if this type is the type of end
     */
    public boolean isEnd() {
        return this == BLOCK_END || this == INLINE_END || this == CONTEXT_END;
    }

    /**
     * Get whether this type is the type of any.
     * @return {@code true}, if this type is the type of any
     */
    public boolean isAny() {
        return this == BLOCK_ANY || this == INLINE_ANY || this == CONTEXT_ANY;
    }

}

