package com.mammb.code.piecetable.buffer;

public interface AppendableBuffer extends Buffer {
    void append(CharSequence cs);
    void append(Buffer buffer);
    void append(byte[] bytes);
}