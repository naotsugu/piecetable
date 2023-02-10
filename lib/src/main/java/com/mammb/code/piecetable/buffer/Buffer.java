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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * The buffer
 * @author Naotsugu Kobayashi
 */
public interface Buffer {

    /**
     * count of code point.
     * @return
     */
    int length();

    byte[] charAt(int index);

    byte[] bytes(int rawStart, int rawEnd);

    byte[] bytes();

    Buffer subBuffer(int start, int end);

    int asIndex(int index);

    default boolean isEmpty() {
        return this.length() == 0;
    }

    default int write(WritableByteChannel channel, ByteBuffer buf,
            int offset, int length) throws IOException {

        int from = asIndex(offset);
        int to   = asIndex(offset + length);

        for (int i = from; i < to;) {
            int m = Math.min(i + buf.remaining(), to);
            buf.put(bytes(i, m));
            buf.flip();
            i += channel.write(buf);
            buf.compact();
        }

        return to - from;
    }

}
