/*
 * Copyright 2019-2023 the original author or authors.
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
