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
 * Token.
 * @param type the type
 * @param scope the scope
 * @param position the position
 * @param length the length
 * @param note the note
 * @author Naotsugu Kobayashi
 */
public record Token(int type, ScopeType scope, int position, int length, String note) {

    /**
     * Constructor.
     * @param type the type
     * @param scope the scope
     * @param position the position
     * @param length the length
     */
    public Token(int type, ScopeType scope, int position, int length) {
        this(type, scope, position, length, "");
    }


    /**
     * Get whether this type is empty.
     * @return {@code true}, if this type is empty
     */
    public boolean isEmpty() {
        return length <= 0;
    }

}
