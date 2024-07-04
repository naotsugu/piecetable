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
package com.mammb.dev.picetable.text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Reader.
 * @author Naotsugu Kobayashi
 */
public class Reader {

    private final Path path;
    private final RowIndex index;
    private int bom = 0;
    private Charset charset = StandardCharsets.UTF_8;
    private long length;

    private Reader(Path path) {
        this.path = path;
        this.index = RowIndex.of();
        if (path != null) {
            readAll(path);
        }
    }

    public static Reader of(Path path) {
        return new Reader(path);
    }

    public RowIndex index() {
        return index;
    }

    public Charset charset() {
        return charset;
    }

    public int bom() {
        return bom;
    }

    private void readAll(Path path) {

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            long size = channel.size();

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
                byte[] read = asBytes(buf, n, bytes);
                if (length == 0) {
                    bom = checkBom(read);
                }
                length += read.length;
                index.add(read);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] asBytes(ByteBuffer buf, int nRead, byte[] bytes) {
        if (buf.isDirect()) {
            if (nRead != bytes.length) {
                byte[] rest = new byte[nRead];
                buf.get(rest);
                return rest;
            } else {
                buf.get(bytes);
                return bytes;
            }
        } else {
            return Arrays.copyOf(buf.array(), buf.limit());
        }
    }

    private int checkBom(byte[] bytes) {
        return 0;
    }

}
