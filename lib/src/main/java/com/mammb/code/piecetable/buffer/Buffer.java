package com.mammb.code.piecetable.buffer;

/**
 * The buffer
 * @author Naotsugu Kobayashi
 */
public interface Buffer {

    int length();

    byte[] charAt(int index);

    byte[] bytes();

    Buffer subBuffer(int start, int end);

    default boolean isEmpty() {
        return this.length() == 0;
    }

}
