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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

/**
 * Memory mapped buffer.
 * @author Naotsugu Kobayashi
 */
public class MappedBuffer implements Buffer, Closeable {

    /** The chunk size. */
    private final int chunkSize = 1_073_741_824;
    /** The mapped byte buffers. */
    private MappedByteBuffer[] maps;
    /** The current size of entity to which this channel is connected. */
    private long length;

    /**
     * Create a new {@link MappedBuffer}.
     * @param file the source file
     */
    private MappedBuffer(RandomAccessFile file) {
        try (FileChannel fc = file.getChannel(); FileLock lock = fc.tryLock()) {
            if (lock == null) throw new RuntimeException("locked:" + file);
            length = fc.size();
            maps = new MappedByteBuffer[1 + (int) (length / chunkSize)];
            for (int i = 0; i < maps.length; i++) {
                long s = (long) chunkSize * i;
                maps[i] = fc.map(FileChannel.MapMode.READ_ONLY, s, Math.min(chunkSize, length - s));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new {@code MapBuffer} from the given {@code Path}.
     * @param path the given {@code Path}
     * @return a new {@code ChannelBuffer}
     */
    public static MappedBuffer of(Path path) {
        try (var file = new RandomAccessFile(path.toFile(), "rw")) {
            return new MappedBuffer(file);
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
        return maps[Math.toIntExact(index / chunkSize)].get(Math.toIntExact(index % chunkSize));
    }

    @Override
    public byte[] bytes(long startIndex, long endIndex) {
        if (startIndex < 0 || endIndex > length || startIndex > endIndex) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(startIndex, endIndex, length));
        }
        byte[] bytes = new byte[Math.toIntExact(endIndex - startIndex)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = get(startIndex + i);
        }
        return bytes;
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
        maps = null;
        length = 0;
        System.gc();
    }

}
