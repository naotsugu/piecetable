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
import com.mammb.code.piecetable.Document;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Parallel reader.
 * @author Naotsugu Kobayashi
 */
public class ParallelReader implements Reader {

    /** The chunk size. */
    static final int CHUNK_SIZE = 1024 * 256;

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
    private final Document.ProgressListener<Long> progressListener;

    /**
     * Constructor.
     * @param path the path to be read
     * @param progressListener the read callback
     * @param matches the CharsetMatches
     */
    ParallelReader(Path path,
            Document.ProgressListener<Long> progressListener,
            CharsetMatch... matches) {
        this.progressListener = progressListener;
        this.matches.addAll(Arrays.asList(matches));
        this.index = RowIndex.of();
        if (path != null && Files.exists(path)) {
            read(path);
            index.trimToSize();
        }
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
        try (var arena = Arena.ofShared(); // parallel needs ofShared arena
             var channel = FileChannel.open(path, StandardOpenOption.READ)) {

            length = channel.size();
            MemorySegment seg = channel.map(FileChannel.MapMode.READ_ONLY, 0, length, arena);

            IntStream.rangeClosed(0, Math.toIntExact(length / CHUNK_SIZE)).parallel()
                .mapToObj(i -> chunkReadOf(i, CHUNK_SIZE, seg))
                .forEachOrdered(this::aggregate);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void aggregate(ChunkRead chunkRead) {
        index.add(chunkRead.rows);
        crCount += chunkRead.crCount;
        lfCount += chunkRead.lfCount;
        if (progressListener != null) {
            boolean continuation = progressListener.accept(chunkRead.byteSize);
            if (!continuation) throw new RuntimeException("interrupted.");
        }
    }

    private ChunkRead chunkReadOf(int chunkNo, int chunkSize, MemorySegment seg) {

        long offset = (long) chunkNo * chunkSize;
        long sliceSize = Math.min(chunkSize, seg.byteSize() - offset);
        byte[] bytes = seg.asSlice(offset, sliceSize).toArray(ValueLayout.JAVA_BYTE);

        if (chunkNo == 0) {
            bytes = handleHeadChunk(bytes);
        }

        IntArray rows = IntArray.of();
        int crCount = 0;
        int lfCount = 0;
        int count = 0;
        for (byte b : bytes) {
            count++;
            if (b == '\r') {
                crCount++;
            } else if (b == '\n') {
                lfCount++;
                rows.add(count);
                count = 0;
            }
        }
        rows.add(count);
        return new ChunkRead(bytes.length, rows.get(), crCount, lfCount);
    }

    private byte[] handleHeadChunk(byte[] bytes) {
        bom = Bom.extract(bytes);
        if (bom.length > 0) {
            charset = Bom.toCharset(bom);
            // exclude BOM
            bytes = Arrays.copyOfRange(bytes, bom.length, bytes.length);
        }
        charset = checkCharset(bytes);
        return bytes;
    }

    record ChunkRead(long byteSize, int[] rows, int crCount, int lfCount) {}

    private Charset checkCharset(byte[] bytes) {
        return matches.stream().map(m -> m.put(bytes))
            .max(Comparator.naturalOrder())
            .filter(r -> r.confidence() >= 100)
            .map(CharsetMatch.Result::charset)
            .orElse(null);
    }

}
