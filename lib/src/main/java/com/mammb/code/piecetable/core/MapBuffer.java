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
public class MapBuffer implements Buffer, Closeable {

    /** The chunk size. */
    private final int chunkSize = 1_073_741_824;
    /** The mapped byte buffers. */
    private MappedByteBuffer[] map;
    /** The current size of entity to which this channel is connected. */
    private long length;

    /**
     * Create a new {@link MapBuffer}.
     * @param file the source file
     */
    private MapBuffer(RandomAccessFile file) {
        try (FileChannel fc = file.getChannel(); FileLock lock = fc.tryLock()) {
            if (lock == null) throw new RuntimeException("locked:" + file);
            length = fc.size();
            map = new MappedByteBuffer[1 + (int) (length / chunkSize)];
            for (int i = 0; i < map.length; i++) {
                long s = (long) chunkSize * i;
                map[i] = fc.map(FileChannel.MapMode.READ_ONLY, s, Math.min(chunkSize, length - s));
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
    public static MapBuffer of(Path path) {
        try (var file = new RandomAccessFile(path.toFile(), "rw")) {
            return new MapBuffer(file);
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
        return map[Math.toIntExact(index / chunkSize)].get(Math.toIntExact(index % chunkSize));
    }

    @Override
    public byte[] bytes(long startIndex, long endIndex) {
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
        map = null;
        length = 0;
        System.gc();
    }

}
