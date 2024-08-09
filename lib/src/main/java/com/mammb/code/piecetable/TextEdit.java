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
package com.mammb.code.piecetable;

import com.mammb.code.piecetable.edit.TextEditImpl;
import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * The text edit.
 * @author Naotsugu Kobayashi
 */
public interface TextEdit {

    /**
     * Create a new {@link TextEdit}.
     * @return a new {@link TextEdit}
     */
    static TextEdit of() {
        return new TextEditImpl(Document.of());
    }

    /**
     * Create a new {@link TextEdit}.
     * @param path the path of the file to read
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path) {
        return new TextEditImpl(DocumentImpl.of(path));
    }

    /**
     * Create a new {@link TextEdit}.
     * @param path the path of the file to read
     * @param charset the charset
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path, Charset charset) {
        return new TextEditImpl(DocumentImpl.of(path, charset));
    }

    /**
     * Create a new {@link TextEdit}.
     * @param path the path of the file to read
     * @param charsetMatches the charset matches
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path, CharsetMatch... charsetMatches) {
        return new TextEditImpl(DocumentImpl.of(path, charsetMatches));
    }

}
