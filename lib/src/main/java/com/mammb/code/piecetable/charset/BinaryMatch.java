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
package com.mammb.code.piecetable.charset;

import com.mammb.code.piecetable.CharsetMatch;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * The BinaryMatch class is an implementation of the {@link CharsetMatch} interface.
 * It analyzes a byte array to determine if it matches the Binary character set.
 * The matching confidence increases when the byte array contains a null byte (0x00).
 * @author Naotsugu Kobayashi
 */
public class BinaryMatch implements CharsetMatch {

    /** The logger. */
    private static final System.Logger log = System.getLogger(BinaryMatch.class.getName());
    /** The result. */
    private final CharsetMatchResult result = new CharsetMatchResult(new BinaryCharset());

    /**
     * Constructor.
     */
    public BinaryMatch() {
        // set a handicap
        result.decreasesConfidence(3);
    }

    @Override
    public Result put(byte[] bytes) {
        for (byte aByte : bytes) {
            if (aByte == 0x00) {
                result.increasesConfidence();
            }
            if (result.confidence() == 100) break;
        }
        log.log(DEBUG, result);
        return result;
    }

    private static class BinaryCharset extends Charset {
        BinaryCharset() {
            super("Binary", null);
        }
        @Override
        public boolean contains(Charset cs) {
            return cs instanceof BinaryCharset;
        }
        @Override
        public CharsetDecoder newDecoder() {
            return new BinaryDecoder(this);
        }
        @Override
        public CharsetEncoder newEncoder() {
            return new BinaryEncoder(this);
        }
    }

    private static class BinaryDecoder extends CharsetDecoder {
        private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
        BinaryDecoder(Charset cs) {
            super(cs, 2.0f, 2.0f);
        }
        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining()) {
                byte b = in.get();
                int v = b & 0xFF;
                out.put(HEX_ARRAY[v >>> 4]);
                out.put(HEX_ARRAY[v & 0x0F]);
            }
            return CoderResult.UNDERFLOW;
        }
    }

    private static class BinaryEncoder extends CharsetEncoder {
        BinaryEncoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.remaining() >= 2) {
                int high = Character.digit(in.get(), 16);
                int low = Character.digit(in.get(), 16);
                if (high == -1 || low == -1) {
                    return CoderResult.malformedForLength(1);
                }
                out.put((byte) ((high << 4) | low));
            }
            return CoderResult.UNDERFLOW;
        }
    }

}
