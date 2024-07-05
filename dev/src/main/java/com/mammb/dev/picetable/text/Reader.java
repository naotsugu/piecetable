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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Reader.
 * @author Naotsugu Kobayashi
 */
public class Reader {

    private final RowIndex index;
    private int bom = 0;
    private Charset charset;
    private long length;
    private final List<CharsetMatch> matches = new ArrayList<>();

    private Reader(Path path, CharsetMatch... matches) {
        this.index = RowIndex.of();
        this.matches.addAll(Arrays.asList(matches));
        if (path != null) {
            readAll(path);
        }
    }

    public static Reader of(Path path) {
        return new Reader(path, CharsetMatch.utf8());
    }

    public static Reader of(Path path, Charset charset) {
        return new Reader(path, CharsetMatch.of(charset));
    }

    public static Reader of(Path path, CharsetMatch... matches) {
        return new Reader(path, matches);
    }

    public RowIndex index() {
        return index;
    }

    public Charset charset() {
        return (charset == null) ? StandardCharsets.UTF_8 : charset;
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
                if (charset == null) {
                    charset = checkCharset(read);
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
        if (bytes == null) {
            return 0;
        } else if (bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xef &&
            (bytes[1] & 0xFF) == 0xbb &&
            (bytes[2] & 0xFF) == 0xbf) {
            charset = StandardCharsets.UTF_8;
            return 3;
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xfe &&
            (bytes[1] & 0xFF) == 0xff) {
            charset = StandardCharsets.UTF_16BE;
            return 2;
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe &&
            (bytes[1] & 0xFF) == 0x00 &&
            (bytes[1] & 0xFF) == 0x00) {
            charset = Charset.forName("UTF_32LE");
            return 4;
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe) {
            charset = StandardCharsets.UTF_16LE;
            return 2;
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0x00 &&
            (bytes[1] & 0xFF) == 0x00 &&
            (bytes[1] & 0xFF) == 0xfe &&
            (bytes[1] & 0xFF) == 0xff) {
            charset = Charset.forName("UTF_32BE");
            return 4;
        }
        return 0;
    }

    private Charset checkCharset(byte[] bytes) {
        return matches.stream().map(m -> m.put(bytes))
            .max(Comparator.naturalOrder())
            .filter(r -> r.confidence() > 100)
            .map(r -> r.charset())
            .orElse(null);
    }

}
