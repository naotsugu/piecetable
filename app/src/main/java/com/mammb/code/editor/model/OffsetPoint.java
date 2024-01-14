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
package com.mammb.code.editor.model;

/**
 * OffsetPoint.
 * @author Naotsugu Kobayashi
 */
public record OffsetPoint(int origin, int offset) {

    /** The empty offset point. */
    public static final OffsetPoint empty = new OffsetPoint(-1, -1);


    /**
     * Get the position.
     * @return the position
     */
    public int position() {
        return origin + offset;
    }


    /**
     * Get whether this OffsetPoint is empty.
     * @return {@code true}, if this OffsetPoint is empty
     */
    public boolean isEmpty() {
        return this == empty;
    }

}
