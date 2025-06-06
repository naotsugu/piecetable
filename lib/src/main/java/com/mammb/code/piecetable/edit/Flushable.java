/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.edit;

/**
 * Represents a mechanism for flushing data or clearing internal state.
 * Classes implementing this interface provide the ability to persist
 * or finalize pending changes, ensuring that the internal state and
 * resources are synchronized or cleaned up appropriately.
 * @author Naotsugu Kobayashi
 */
public interface Flushable {

    /**
     * Flush.
     */
    void flush();

}
