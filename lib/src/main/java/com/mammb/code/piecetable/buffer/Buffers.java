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
public interface Buffers {

    static Buffer of(CharSequence cs) {
        return of(cs.toString().getBytes(Utf8.charset()));
    }

    static Buffer of(byte[] bytes) {
        return ReadBuffer.of(bytes);
    }

    static Buffer of(SeekableByteChannel channel) {
        return ChannelBuffer.of(channel);
    }

    static Buffer of(Path path) {
        try {
            return (Files.size(path) >= Runtime.getRuntime().freeMemory() * 0.8)
                ? ChannelBuffer.of(FileChannel.open(path, StandardOpenOption.READ))
                : ReadBuffer.of(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static AppendBuffer appendOf() {
        return GrowBuffer.of();
    }

}
