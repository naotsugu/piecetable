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
import com.mammb.code.piecetable.Progress;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
public class SeqReader implements Reader {

    /** The row index. */
    private final RowIndex index;
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
    /** The read callback. */
    private final Progress.Listener<Void> progressListener;

    /**
     * Constructor.
     * @param path the path to be read
     * @param rowPrefLimit the limit of row
     * @param progressListener the read callback
     * @param matches the CharsetMatches
     */
    SeqReader(Path path, int rowPrefLimit,
            Progress.Listener<Void> progressListener,
            CharsetMatch... matches) {
        this.progressListener = progressListener;
        this.matches.addAll(Arrays.asList(matches));
        this.index = RowIndex.of();
        read(path, rowPrefLimit);
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
     * @param rowPrefLimit the limit on the number of rows to read from the file,
     * if -1 is specified, there is no limit.
     */
    void read(Path path, int rowPrefLimit) {

        if (path == null || !Files.exists(path)) return;

        var listener = progressListener;

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
            long nRead = 0;

            for (;;) {

                if (Thread.interrupted())
                    throw new RuntimeException("interrupted");

                buf.clear();
                int n = channel.read(buf);
                if (n < 0) {
                    break;
                }
                buf.flip();
                byte[] read = asBytes(buf, n, bytes);
                nRead += read.length;

                if (length == 0) {
                    bom = Bom.extract(read);
                    if (bom.length > 0) {
                        charset = Bom.toCharset(bom);
                        // exclude BOM
                        read = Arrays.copyOfRange(read, bom.length, read.length);
                    }
                }
                if (charset == null) {
                    charset = checkCharset(read);
                }
                length += read.length;
                for (byte b : read) {
                    if (b == '\r') crCount++;
                    else if (b == '\n') lfCount++;
                }

                index.add(read);

                if (listener != null) {
                    var progress = Progress.of(nRead, length);
                    boolean continuation = listener.accept(progress);
                    if (!continuation) break;
                }

                if (rowPrefLimit >= 0 && (rowPrefLimit < crCount || rowPrefLimit < lfCount)) {
                    break;
                }
            }

            index.buildStCache();

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

    private Charset checkCharset(byte[] bytes) {
        return matches.stream().map(m -> m.put(bytes))
            .max(Comparator.naturalOrder())
            .map(CharsetMatch.Result::charset)
            .orElse(null);
    }

}
