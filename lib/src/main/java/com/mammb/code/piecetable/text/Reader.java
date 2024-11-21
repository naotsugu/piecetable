/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.CharsetMatch;
import com.mammb.code.piecetable.DocumentStat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Reader.
 * @author Naotsugu Kobayashi
 */
public class Reader implements DocumentStat {

    /** The row index. */
    private final RowIndex index = RowIndex.of();
    /** The byte order mark. */
    private byte[] bom = new byte[0];
    /** The charset read. */
    private Charset charset;
    /** The byte length read. */
    private long length = 0;
    /** The CharsetMatches. */
    private final List<CharsetMatch> matches = new ArrayList<>();
    /** The count of carriage return. */
    private int crCount = 0;
    /** The count of line feed. */
    private int lfCount = 0;

    /**
     * Constructor.
     * @param path the path to be read
     * @param matches the CharsetMatches
     */
    private Reader(Path path, CharsetMatch... matches) {
        this(path, -1, matches);
    }

    /**
     * Constructor.
     * @param path the path to be read
     * @param rowLimit the limit of row
     * @param matches the CharsetMatches
     */
    private Reader(Path path, int rowLimit, CharsetMatch... matches) {
        this.matches.addAll(Arrays.asList(matches));
        if (path != null) {
            read(path, rowLimit);
        }
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @return a new {@link Reader}.
     */
    public static Reader of(Path path) {
        return new Reader(path, CharsetMatches.utf8(), CharsetMatches.ms932());
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @param charset the character set of the file to be read
     * @return a new {@link Reader}.
     */
    public static Reader of(Path path, Charset charset) {
        return new Reader(path, CharsetMatch.of(charset));
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @param rowLimit the limit of row
     * @param matches the {@link CharsetMatch} used in reading the target file
     * @return a new {@link Reader}.
     */
    public static Reader of(Path path, int rowLimit, CharsetMatch... matches) {
        return new Reader(path, rowLimit, matches);
    }

    /**
     * Get the {@link RowIndex}.
     * @return the {@link RowIndex}
     */
    public RowIndex index() {
        return index;
    }

    @Override
    public Charset charset() {
        return (charset == null) ? StandardCharsets.UTF_8 : charset;
    }

    @Override
    public int crCount() {
        return crCount;
    }

    @Override
    public int lfCount() {
        return lfCount;
    }

    @Override
    public byte[] bom() {
        return bom;
    }

    /**
     * Gets the row lengths array.
     * @return the row lengths array
     */
    public int[] rowLengths() {
        return index.rowLengths();
    }

    /**
     * Read the file at the specified path.
     * @param path the path of file to be reade
     * @param rowLimit the limit on the number of rows to read from the file,
     *                 if -1 is specified, there is no limit
     */
    private void read(Path path, int rowLimit) {

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            long size = channel.size();
            if (size <= 0) {
                return;
            }

            int cap = 1024 * 64;
            ByteBuffer buf = (size < cap)
                ? ByteBuffer.allocate((int) size)
                : ByteBuffer.allocateDirect(cap);

            byte[] bytes = new byte[buf.capacity()];

            for (;;) {
                buf.clear();
                int n = channel.read(buf);
                if (n < 0) {
                    break;
                }

                buf.flip();

                byte[] read = asBytes(buf, n, bytes);
                if (length == 0) {
                    bom = checkBom(read);
                }
                if (charset == null) {
                    charset = checkCharset(read);
                }
                length += read.length;
                index.add(read);
                for (byte b : read) {
                    if (b == '\r') crCount++;
                    else if (b == '\n') lfCount++;
                }

                if (rowLimit >= 0 && (rowLimit < crCount || rowLimit < lfCount)) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private byte[] asBytes(ByteBuffer buf, int nRead, byte[] bytes) {
        if (buf.isDirect()) {
            if (nRead != bytes.length) {
                byte[] rest = new byte[nRead];
                buf.get(rest);
                return rest;
            } else {
                buf.get(bytes);
                return bytes;
            }
        } else {
            return Arrays.copyOf(buf.array(), buf.limit());
        }
    }


    private byte[] checkBom(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        } else if (bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xef &&
            (bytes[1] & 0xFF) == 0xbb &&
            (bytes[2] & 0xFF) == 0xbf) {
            charset = StandardCharsets.UTF_8;
            return new byte[] { bytes[0], bytes[1], bytes[2] };
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xfe &&
            (bytes[1] & 0xFF) == 0xff) {
            charset = StandardCharsets.UTF_16BE;
            return new byte[] { bytes[0], bytes[1] };
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe &&
            (bytes[2] & 0xFF) == 0x00 &&
            (bytes[3] & 0xFF) == 0x00) {
            charset = Charset.forName("UTF_32LE");
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] };
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe) {
            charset = StandardCharsets.UTF_16LE;
            return new byte[] { bytes[0], bytes[1] };
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0x00 &&
            (bytes[1] & 0xFF) == 0x00 &&
            (bytes[2] & 0xFF) == 0xfe &&
            (bytes[3] & 0xFF) == 0xff) {
            charset = Charset.forName("UTF_32BE");
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] };
        }
        return new byte[0];
    }


    private Charset checkCharset(byte[] bytes) {
        return matches.stream().map(m -> m.put(bytes))
            .max(Comparator.naturalOrder())
            .filter(r -> r.confidence() >= 100)
            .map(CharsetMatch.Result::charset)
            .orElse(null);
    }

}
