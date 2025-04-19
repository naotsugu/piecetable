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
package com.mammb.code.piecetable.charset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Bom utility.
 * @author Naotsugu Kobayashi
 */
public interface Bom {

    /** utf-8 bom. */
    byte[] UTF_8    = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
    /** utf-16BE bom. */
    byte[] UTF_16BE = new byte[] { (byte) 0xfe, (byte) 0xff };
    /** utf-16LE bom. */
    byte[] UTF_16LE = new byte[] { (byte) 0xff, (byte) 0xfe };
    /** utf-32BE bom. */
    byte[] UTF_32BE = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xff };
    /** utf-32LE bom. */
    byte[] UTF_32LE = new byte[] { (byte) 0xff, (byte) 0xfe, (byte) 0x00, (byte) 0x00 };
    /** GB18030 bom. */
    byte[] GB18030  = new byte[] { (byte) 0x84, (byte) 0x31, (byte) 0x95, (byte) 0x33 };

    /**
     * Extracts the bom from the specified byte array.
     * @param bytes the specified byte array
     * @return the bom
     */
    static byte[] extract(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        } else if (bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xef &&
            (bytes[1] & 0xFF) == 0xbb &&
            (bytes[2] & 0xFF) == 0xbf) {
            return new byte[] { bytes[0], bytes[1], bytes[2] }; // UTF_8
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xfe &&
            (bytes[1] & 0xFF) == 0xff) {
            return new byte[] { bytes[0], bytes[1] }; // UTF_16BE
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe &&
            (bytes[2] & 0xFF) == 0x00 &&
            (bytes[3] & 0xFF) == 0x00) {
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] }; // UTF_32LE
        } else if (bytes.length >= 2 &&
            (bytes[0] & 0xFF) == 0xff &&
            (bytes[1] & 0xFF) == 0xfe) {
            return new byte[] { bytes[0], bytes[1] }; // UTF_16LE
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0x00 &&
            (bytes[1] & 0xFF) == 0x00 &&
            (bytes[2] & 0xFF) == 0xfe &&
            (bytes[3] & 0xFF) == 0xff) {
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] }; // UTF_32BE
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0x84 &&
            (bytes[1] & 0xFF) == 0x31 &&
            (bytes[2] & 0xFF) == 0x95 &&
            (bytes[3] & 0xFF) == 0x33) {
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] }; // GB18030
        } else if (bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xf7 &&
            (bytes[1] & 0xFF) == 0x64 &&
            (bytes[2] & 0xFF) == 0x4c) {
            return new byte[] { bytes[0], bytes[1], bytes[2] }; // UTF-1
        } else if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0xdd &&
            (bytes[1] & 0xFF) == 0x73 &&
            (bytes[2] & 0xFF) == 0x66 &&
            (bytes[3] & 0xFF) == 0x73) {
            return new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] }; //UTF-EBCDIC
        }
        return new byte[0];
    }

    /**
     * Get the character set from the specified bom.
     * @param bom the bom byte array
     * @return the character
     */
    static Charset toCharset(byte[] bom) {
        if (Arrays.equals(bom, UTF_8)) {
            return StandardCharsets.UTF_8;
        } else if (Arrays.equals(bom, UTF_16BE)) {
            return StandardCharsets.UTF_16BE;
        } else if (Arrays.equals(bom, UTF_32LE)) {
            return Charset.forName("UTF_32LE");
        } else if (Arrays.equals(bom, UTF_16LE)) {
            return StandardCharsets.UTF_16LE;
        } else if (Arrays.equals(bom, UTF_32BE)) {
            return Charset.forName("UTF_32BE");
        } else if (Arrays.equals(bom, GB18030)) {
            return Charset.forName("GB18030");
        } else {
            return null;
        }
    }

}
