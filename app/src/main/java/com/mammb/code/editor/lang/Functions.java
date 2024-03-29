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
package com.mammb.code.editor.lang;

/**
 * Function utility.
 * @author Naotsugu Kobayashi
 */
public class Functions {

    /**
     * Get a runnable that performs, in sequence.
     * @param before the operation to perform before this operation
     * @param after the operation to perform after this operation
     * @return a composed Runnable that performs in sequence
     */
    public static Runnable and(Runnable before, Runnable after) {
        return () -> {
            if (before != null) before.run();
            if (after  != null) after.run();
        };
    }


    /**
     * Get a empty runnable.
     * @return a empty runnable
     */
    public static Runnable empty() {
        return () -> { };
    }

}
