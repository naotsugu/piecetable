package com.mammb.code.piecetable.buffer;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utility of buffers.
 * @author Naotsugu Kobayashi
 */
public abstract class Buffers {

    private Buffers() { }

    public static Buffer of(CharSequence cs) {
        return of(cs.toString().getBytes(Utf8.charset()));
    }

    public static Buffer of(byte[] bytes) {
        return ReadBuffer.of(bytes);
    }

    public static Buffer of(SeekableByteChannel channel) {
        return ChannelBuffer.of(channel);
    }

    public static Buffer of(Path path) {
        try {
            return (Files.size(path) >= Runtime.getRuntime().freeMemory() * 0.8)
                ? ChannelBuffer.of(FileChannel.open(path, StandardOpenOption.READ))
                : ReadBuffer.of(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AppendBuffer appendOf() {
        return GrowBuffer.of();
    }

}
