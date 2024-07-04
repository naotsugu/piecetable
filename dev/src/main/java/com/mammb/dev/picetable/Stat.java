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
package com.mammb.dev.picetable;

import com.mammb.dev.picetable.index.RowIndex;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;

public class Stat {

    private RowIndex index;

    private Stat(Path path) {
        this.index = RowIndex.of();
        if (path != null) {
            readAll(path, index::add);
        }
    }

    public static Stat of(Path path) {
        return new Stat(path);
    }

    public RowIndex index() {
        return index;
    }

    private void readAll(Path path, Consumer<byte[]> consumer) {

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            long size = channel.size();
            if (size > (long) Integer.MAX_VALUE) {
                throw new OutOfMemoryError("Required array size too large");
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

                if (buf.isDirect()) {
                    if (n != bytes.length) {
                        byte[] rest = new byte[n];
                        buf.get(rest);
                        consumer.accept(rest);
                    } else {
                        buf.get(bytes);
                        consumer.accept(bytes);
                    }
                } else {
                    consumer.accept(Arrays.copyOf(buf.array(), buf.limit()));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
