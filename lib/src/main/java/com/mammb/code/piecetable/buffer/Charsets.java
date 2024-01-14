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
package com.mammb.code.piecetable.buffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

/**
 * The utility of charset.
 * @author Naotsugu Kobayashi
 */
public class Charsets {

    /** The size of buffer. */
    private static final int bufSize = 8000;


    /**
     * Detect charset.
     * @param source the source input stream
     * @return the charset
     */
    public static Charset charsetOf(InputStream source) {

        final InputStream is = (source.markSupported()) ? source
            : new BufferedInputStream(source);

        byte[] bytes = new byte[bufSize];
        is.mark(bufSize);

        try {
            int len = 0;
            int remaining = bufSize;
            while (remaining > 0 ) {
                int bytesRead = is.read(bytes, len, remaining);
                if (bytesRead <= 0) {
                    break;
                }
                len += bytesRead;
                remaining -= bytesRead;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { is.reset(); } catch (IOException ignore) { }
        }

        if (isUtf8(bytes)) {
            return StandardCharsets.UTF_8;
        }
        if (isUtf16BE(bytes)) {
            return StandardCharsets.UTF_16BE;
        }
        if (isUtf16LE(bytes)) {
            return StandardCharsets.UTF_16LE;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        String[] charsets = {"windows-31j", "ISO-2022-JP"};
        for (String charsetName : charsets) {
            Charset cs = Charset.forName(charsetName);
            buffer.clear();
            if (canDecode(buffer, cs)) {
                return cs;
            }
        }

        for (Charset cs : Charset.availableCharsets().values()) {
            buffer.clear();
            if (canDecode(buffer, cs)) return cs;
        }

        return Charset.defaultCharset();

    }

    private static boolean isUtf8(byte[] bytes) {

        if (hasUtf8Bom(bytes)) return true;

        int valid   = 0;
        int invalid = 0;
        int trailBytes = 0;

        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if ((b & 0x80) == 0) { // ASCII
                continue;
            }

            if ((b & 0x0e0) == 0x0c0) {
                trailBytes = 1;
            } else if ((b & 0x0f0) == 0x0e0) {
                trailBytes = 2;
            } else if ((b & 0x0f8) == 0xf0) {
                trailBytes = 3;
            } else {
                invalid++;
                continue;
            }

            for (;;) {
                i++;
                if ( i >= bytes.length) {
                    break;
                }
                b = bytes[i];
                if ((b & 0xc0) != 0x080) {
                    invalid++;
                    break;
                }
                if (--trailBytes == 0) {
                    valid++;
                    break;
                }
            }
        }
        return invalid == 0;
    }

    private static boolean isUtf16BE(byte[] bytes) {

        int confidence = 10;
        int bytesToCheck = Math.min(bytes.length, 30);

        for (int i = 0; i < bytesToCheck - 1; i += 2) {
            int codeUnit = codeUnit16FromBytes(bytes[i], bytes[i + 1]);
            if (i == 0 && codeUnit == 0xFEFF) { // BOM : BE 0xFE 0xFF
                return true;
            }
            confidence = adjustConfidence(codeUnit, confidence);
            if (confidence == 0 || confidence == 100) {
                break;
            }
        }
        if (bytesToCheck < 4 && confidence < 100) {
            return false;
        }
        return confidence == 100;
    }

    private static boolean isUtf16LE(byte[] bytes) {

        int confidence = 10;
        int bytesToCheck = Math.min(bytes.length, 30);

        for (int charIndex = 0; charIndex < bytesToCheck - 1; charIndex += 2) {
            int codeUnit = codeUnit16FromBytes(bytes[charIndex + 1], bytes[charIndex]);
            if (charIndex == 0 && codeUnit == 0xFEFF) { // BOM : LE 0xFF 0xFE
                return true;
            }
            confidence = adjustConfidence(codeUnit, confidence);
            if (confidence == 0 || confidence == 100) {
                break;
            }
        }
        if (bytesToCheck < 4 && confidence < 100) {
            return false;
        }
        return confidence == 100;
    }

    private static boolean hasUtf8Bom(byte[] bytes) {
        return (bytes != null && bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xef && (bytes[1] & 0xFF) == 0xbb && (bytes[2] & 0xFF) == 0xbf);
    }

    private static int codeUnit16FromBytes(byte hi, byte lo) {
        return ((hi & 0xff) << 8) | (lo & 0xff);
    }

    private static int adjustConfidence(int codeUnit, int confidence) {
        if (codeUnit == 0) {
            confidence -= 10;
        } else if ((codeUnit >= 0x20 && codeUnit <= 0xff) || codeUnit == 0x0a) {
            confidence += 10;
        }
        if (confidence < 0) {
            confidence = 0;
        } else if (confidence > 100) {
            confidence = 100;
        }
        return confidence;
    }


    private static boolean canDecode(ByteBuffer bytes, Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();
            decoder.decode(bytes);
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }

}
