package com.mammb.code.editor3.syntax;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Source {

    /** The input string. */
    private InputStream input;

    /** The current position in input (points to current char). */
    private int position = -1;

    private int peekCount = 0;


    private Source(InputStream input) {
        this.input = input.markSupported() ? input : new BufferedInputStream(input);
    }


    public static Source of(InputStream input) {
        return new Source(input);
    }


    public static Source of(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_16));
        try {
            // skip BOM(Byte Order Mark) fe, ff
            in.skipNBytes(2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Source(in);
    }


    public char readChar() {
        int read = read();
        return (read == -1) ? 0 : (char) read;
    }


    public char peekChar() {
        int peek = peek();
        return (peek == -1) ? 0 : (char) peek;
    }


    public void commitPeek() {
        try {
            if (peekCount > 0) {
                input.reset();
                input.skipNBytes(peekCount * 2);
                peekCount = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public int position() { return position; }


    private int read() {
        try {
            if (peekCount > 0) {
                input.reset();
                peekCount = 0;
            }
            position++;
            byte[] bytes = new byte[2];
            int ret = input.read(bytes);
            if (ret < 2) {
                return -1;
            }
            return bytesToChar(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private int peek() {
        try {
            if (peekCount == 0) {
                input.mark(Integer.MAX_VALUE);
            }
            peekCount++;
            byte[] bytes = new byte[2];
            int ret = input.read(bytes);
            if (ret < 2) {
                return -1;
            }
            return bytesToChar(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static char bytesToChar(byte[] b) {
        return (char) ((b[0] << 8) & 0xFF00 | (b[1] & 0x00FF));
    }

}
