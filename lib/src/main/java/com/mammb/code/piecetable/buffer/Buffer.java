package com.mammb.code.piecetable.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * The buffer
 * @author Naotsugu Kobayashi
 */
public interface Buffer {

    int length();

    byte[] charAt(int index);

    byte[] bytes();

    Buffer subBuffer(int start, int end);

    int asIndex(int index);

    default boolean isEmpty() {
        return this.length() == 0;
    }

    default void write(WritableByteChannel channel, ByteBuffer buf,
            int offset, int length) throws IOException {

        int from = asIndex(offset);
        int to   = asIndex(offset + length);

        for (int i = from; i < to;) {
            int m = Math.min(i + buf.remaining(), to);
            buf.put(Arrays.copyOfRange(bytes(), i, m));
            buf.flip();
            i += channel.write(buf);
            buf.compact();
        }
    }

}
