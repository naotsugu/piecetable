package com.mammb.code.piecetable.buffer;

public interface Buffers {

    static Buffer of(CharSequence cs) {
        return of(cs.toString().getBytes(Utf8.charset()));
    }

    static Buffer of(byte[] bytes) {
        return ReadBuffer.of(bytes);
    }

    static AppendableBuffer appendableOf() {
        return AppendBuffer.of();
    }

}
