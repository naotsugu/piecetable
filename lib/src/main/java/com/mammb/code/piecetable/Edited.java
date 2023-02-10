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
package com.mammb.code.piecetable;

import java.util.Objects;

/**
 * Edited record.
 * @param type edit type
 * @param pos edit start point
 * @param len length
 * @param bytes edited bytes
 * @author Naotsugu Kobayashi
 */
public record Edited(EditType type, int pos, int len, byte[] bytes) {

    public static final Edited empty = new Edited(EditType.NIL, 0, 0, new byte[0]);

    public Edited {
        Objects.requireNonNull(type);
        Objects.requireNonNull(bytes);
    }

    public boolean isInserted() {
        return type.isInsert();
    }

    public boolean isDeleted() {
        return type.isDelete();
    }

    public boolean isEmpty() {
        return type.isNil();
    }

    public int lineCount() {
        if (bytes.length == 0) return 0;
        int count = 1;
        for (byte b : bytes) {
            if (b == '\n') count++;
        }
        return count;
    }
}
