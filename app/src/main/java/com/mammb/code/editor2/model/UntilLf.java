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
package com.mammb.code.editor2.model;

import java.util.function.Predicate;

/**
 * UntilLf.
 * @author Naotsugu Kobayashi
 */
public class UntilLf implements Predicate<byte[]> {

    /** The line feed count. */
    private int count;

    /**
     * Constructor.
     * @param count the line feed count
     */
    private UntilLf(int count) {
        if (count <= 0) throw new IllegalArgumentException();
        this.count = count;
    }

    /**
     * Create a new UntilLf.
     * @param count the line feed count
     * @return the UntilLf predicate
     */
    public static UntilLf of(int count) {
        return new UntilLf(count);
    }

    /**
     * Create a new UntilLf.
     * @return the UntilLf predicate
     */
    public static UntilLf of() {
        return new UntilLf(1);
    }

    @Override
    public boolean test(byte[] bytes) {
        if (bytes != null && bytes.length > 0 && bytes[0] == '\n') {
            count--;
        }
        return count == 0;
    }

}
