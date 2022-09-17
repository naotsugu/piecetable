package com.mammb.code.piecetable.buffer;

/**
 * Utility of buffers.
 * @author Naotsugu Kobayashi
 */
public interface Buffers {

    static Buffer of(CharSequence cs) {
        return of(cs.toString().getBytes(Utf8.charset()));
    }

    static Buffer of(byte[] bytes) {
        return ReadBuffer.of(bytes);
    }

    static AppendBuffer appendOf() {
        return GrowBuffer.of();
    }

}
