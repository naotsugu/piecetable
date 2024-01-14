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
 * EditType.
 * @author Naotsugu Kobayashi
 */
public enum EditType {

    /** insert. */
    INSERT,
    /** delete. */
    DELETE,
    /** nil. */
    NIL;

    /**
     * Get whether this type is insert type.
     * @return {@code true}, if this type is insert type
     */
    public boolean isInsert() { return this == INSERT; }


    /**
     * Get whether this type is delete type.
     * @return {@code true}, if this type is delete type
     */
    public boolean isDelete() { return this == DELETE; }


    /**
     * Get whether this type is nil type.
     * @return {@code true}, if this type is nil type
     */
    public boolean isNil() { return this == NIL; }


    /**
     * Flip type.
     * @return the flipped type.
     */
    public EditType flip() {
        return switch (this) {
            case INSERT -> DELETE;
            case DELETE -> INSERT;
            case NIL -> NIL;
        };
    }

}
