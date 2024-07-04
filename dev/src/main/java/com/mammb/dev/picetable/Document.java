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
package com.mammb.dev.picetable;

import java.nio.charset.Charset;

/**
 * The document.
 * @author Naotsugu Kobayashi
 */
public interface Document {

    void insert(int row, int col, CharSequence cs);

    void insert(int row, int col, byte[] bytes);

    void delete(int row, int col, int len);

    byte[] get(int row, int col, int len);

    byte[] get(int row);

    /**
     * Get the bytes length of this document holds.
     * @return the bytes length of this document holds
     */
    long length();

    Charset charset();

}
