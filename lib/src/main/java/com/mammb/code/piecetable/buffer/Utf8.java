package com.mammb.code.piecetable.buffer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class Utf8 {

    private Utf8() { }

    public static boolean isSurrogateRetain(byte b) {
        return (b & 0xC0) == 0x80;
    }

    public static short surrogateCount(byte b) {
        if ((b & 0x80) == 0x00) {
            return 1;
        } else if ((b & 0xE0) == 0xC0) {
            return 2;
        } else if ((b & 0xF0) == 0xE0) {
            return 3;
        } else if ((b & 0xF8) == 0xF0) {
            return 4;
        } else {
            throw new IllegalArgumentException(Byte.toString(b));
        }
    }

    public static byte[] asCharBytes(byte[] bytes, int index) {
        byte b = bytes[index];
        return switch (surrogateCount(b)) {
            case 1 -> new byte[] { b };
            case 2 -> new byte[] { b, bytes[index + 1] };
            case 3 -> new byte[] { b, bytes[index + 1], bytes[index + 2] };
            case 4 -> new byte[] { b, bytes[index + 1], bytes[index + 2], bytes[index + 3] };
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    public static Charset charset() {
        return StandardCharsets.UTF_8;
    }

}
