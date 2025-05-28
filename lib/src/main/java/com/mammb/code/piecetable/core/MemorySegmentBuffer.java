/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

/**
 * The {@code MemorySegmentBuffer} class provides a memory-mapped implementation
 * of the {@link Buffer} interface. It allows efficient access to a sequence of bytes
 * directly from a file or memory segment, providing methods for reading data from
 * this buffer.
 *
 * This class uses memory segments for accessing and manipulating bytes and performs
 * operations such as retrieving individual bytes, slices of byte arrays, and reading
 * bytes into buffers. The memory segment is backed by a file and is mapped in a
 * read-only mode. The class also implements {@link Closeable} to ensure proper
 * release of resources when the buffer is no longer in use.
 * @author Naotsugu Kobayashi
 */
public class MemorySegmentBuffer implements Buffer, Closeable {

    /** The arena. */
    private final Arena arena;
    /** The memory segment. */
    private MemorySegment ms;
    /** The current size of the entity to which this channel is connected. */
    private long length;

    /**
     * Create a new {@link MemorySegmentBuffer}.
     * @param file the source file
     */
    public MemorySegmentBuffer(RandomAccessFile file) {
        try (FileChannel fc = file.getChannel(); FileLock lock = fc.tryLock()) {
            if (lock == null) throw new RuntimeException("locked:" + file);
            length = fc.size();
            arena = Arena.ofConfined();
            ms = fc.map(FileChannel.MapMode.READ_ONLY, 0, length, arena);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new {@code MsBuffer} from the given {@code Path}.
     * @param path the given {@code Path}
     * @return a new {@code MsBuffer}
     */
    public static MemorySegmentBuffer of(Path path) {
        try (var file = new RandomAccessFile(path.toFile(), "rw")) {
            return new MemorySegmentBuffer(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte get(long index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length));
        }
        return ms.get(ValueLayout.JAVA_BYTE, index);
    }

    @Override
    public byte[] bytes(long startIndex, long endIndex) {
        if (startIndex < 0 || endIndex > length || startIndex > endIndex) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(startIndex, endIndex, length));
        }
        return ms.asSlice(startIndex, endIndex - startIndex).toArray(ValueLayout.JAVA_BYTE);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public long read(long offset, long length, ByteBuffer buffer) {
        long n = 0;
        for (long i = offset; buffer.hasRemaining(); i++) {
            buffer.put(get(i));
            n++;
            if (n == length) return -1;
        }
        return offset + n;
    }

    @Override
    public void close() {
        arena.close();
    }
}
