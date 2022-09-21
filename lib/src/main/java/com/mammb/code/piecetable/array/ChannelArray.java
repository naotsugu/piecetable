package com.mammb.code.piecetable.array;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * Buffered byte channel.
 */
public class ChannelArray {

    private static final short PREF_BUF_SIZE = 1024 * 4;
    private static final byte[] EMPTY = {};

    private final SeekableByteChannel ch;
    private final int chSize;

    private byte[] buffer;
    private int offset;

    private ChannelArray(SeekableByteChannel ch) {
        try {
            this.chSize = Math.toIntExact(ch.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.ch = ch;
        this.buffer = EMPTY;
        this.offset = 0;
    }

    public static ChannelArray of(SeekableByteChannel ch) {
        return new ChannelArray(ch);
    }

    public byte get(int index) {
        if (buffer == EMPTY || index < offset || index >= offset + buffer.length) {
            fillBuffer(index, Math.addExact(index, PREF_BUF_SIZE));
        }
        return buffer[index - offset];
    }

    public byte[] get(int from, int to) {
        if (buffer == EMPTY || from < offset || to > offset + buffer.length) {
            fillBuffer(from, to);
            if (buffer.length > PREF_BUF_SIZE << 8) {
                // if too large, trim buffer
                byte[] ret = buffer;
                buffer = Arrays.copyOf(buffer, PREF_BUF_SIZE);
                return ret;
            }
        }
        return Arrays.copyOfRange(buffer, from - offset, to - offset);
    }

    public int length() {
        return chSize;
    }

    public void clear() {
        this.buffer = EMPTY;
        this.offset = 0;
    }

    public void close() {
        try {
            ch.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillBuffer(int from, int to) {
        try {
            var bb = ByteBuffer.allocate(
                Math.max(to, Math.addExact(from, PREF_BUF_SIZE) - from));
            ch.position(from);
            ch.read(bb);
            bb.flip();
            buffer = Arrays.copyOf(bb.array(), bb.limit());
            offset = from;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
