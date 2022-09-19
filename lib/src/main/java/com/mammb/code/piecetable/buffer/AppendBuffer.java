package com.mammb.code.piecetable.buffer;

/**
 * Appendable buffer.
 * @author Naotsugu Kobayashi
 */
public interface AppendBuffer extends Buffer {
    void append(CharSequence cs);
    void append(Buffer buffer);
    void append(byte[] bytes);
    void clear();
}
