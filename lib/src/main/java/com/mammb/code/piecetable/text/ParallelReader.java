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
import com.mammb.code.piecetable.Segment;
import com.mammb.code.piecetable.charset.Bom;
import com.mammb.code.piecetable.charset.CharsetMatches;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * ParallelReader is a multithreaded implementation of the {@link Reader} interface for reading large files.
 * It reads data in parallel chunks to efficiently handle large file processing while maintaining file structure and statistics.
 * @author Naotsugu Kobayashi
 */
public class ParallelReader implements Reader {

    /** The chunk size. */
    static final int CHUNK_SIZE = 1024 * 256;

    /** The row index. */
    private RowIndex index;
    /** The byte order mark. */
    private byte[] bom = new byte[0];
    /** The charset read. */
    private volatile Charset charset;
    /** The byte length read. */
    private long length = 0;
    /** The CharsetMatches. */
    private final List<CharsetMatch> matches = new ArrayList<>();
    /** The count of the carriage return. */
    private int crCount = 0;
    /** The count of line feed. */
    private int lfCount = 0;
    /** The read callback. */
    private final Consumer<Segment> progressListener;
    /** The lock object. */
    private final Object lock = new Object();

    /**
     * Constructor.
     * @param path the path to be read
     * @param progressListener the read callback
     * @param matches the CharsetMatches
     */
    ParallelReader(Path path,
            Consumer<Segment> progressListener,
            CharsetMatch... matches) {
        this.progressListener = progressListener;
        this.matches.addAll(Arrays.asList(matches));
        read(path);
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

    private void read(Path path) {

        if (path == null || !Files.exists(path)) return;

        try (var arena = Arena.ofShared(); // parallel needs ofShared arena
             var channel = FileChannel.open(path, StandardOpenOption.READ)) {

            length = channel.size();
            MemorySegment seg = channel.map(FileChannel.MapMode.READ_ONLY, 0, length, arena);

            var chunkRead = chunkReadOf(0, CHUNK_SIZE, seg);
            aggregate(chunkRead);

            IntStream.rangeClosed(1, Math.toIntExact(length / CHUNK_SIZE)).parallel()
                .mapToObj(i -> chunkReadOf(i, CHUNK_SIZE, seg))
                .forEachOrdered(this::aggregate);

            index.buildStCache();
            index.trimToSize();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void aggregate(ChunkRead chunkRead) {

        if (Thread.interrupted())
            throw new RuntimeException("interrupted");

        index.add(chunkRead.rows);
        crCount += chunkRead.crCount;
        lfCount += chunkRead.lfCount;

        if (progressListener != null) {
            progressListener.accept(Segment.of(chunkRead.byteSize, length));
        }
    }

    private ChunkRead chunkReadOf(int chunkNo, int chunkSize, MemorySegment seg) {

        if (Thread.interrupted())
            throw new RuntimeException("interrupted");

        long offset = (long) chunkNo * chunkSize;
        long sliceSize = Math.min(chunkSize, seg.byteSize() - offset);
        byte[] bytes = seg.asSlice(offset, sliceSize).toArray(ValueLayout.JAVA_BYTE);

        if (chunkNo == 0) {
            bytes = handleHeadChunk(bytes);
        }

        IntArray rows = IntArray.of();
        int[] crlf = index.traversRow(bytes, rows);
        return new ChunkRead(chunkNo, bytes.length, rows.get(), crlf[0], crlf[1]);
    }

    private byte[] handleHeadChunk(byte[] bytes) {
        bom = Bom.extract(bytes);
        if (bom.length > 0) {
            charset = Bom.toCharset(bom);
            // exclude BOM
            bytes = Arrays.copyOfRange(bytes, bom.length, bytes.length);
        } else {
            charset = CharsetMatches.estimate(bytes, matches).orElse(StandardCharsets.UTF_8);
        }
        index = RowIndex.of(charset);
        return bytes;
    }

    record ChunkRead(int chunkNo, long byteSize, int[] rows, int crCount, int lfCount) {}

}
