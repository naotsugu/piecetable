package com.mammb.code.piecetable;

import java.nio.charset.StandardCharsets;
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
