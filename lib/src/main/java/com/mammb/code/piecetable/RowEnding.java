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
package com.mammb.code.piecetable;

/**
 * RowEnding.
 * @author Naotsugu Kobayashi
 */
public enum RowEnding {
    /** The line feed. */
    LF,
    /** The carriage return. */
    CR,
    /** CRLF. */
    CRLF,
    ;

    /**
     * The platform line separator.
     */
    public static final RowEnding platform = platform();

    /**
     * Unify line ending.
     *
     * @param cs unify string
     * @return the unified string
     */
    public CharSequence unify(CharSequence cs) {
        return switch (this) {
            case LF -> unifyLf(cs);
            case CR -> unifyCr(cs);
            case CRLF -> unifyCrLf(cs);
        };
    }

    /**
     * Get the line ending string.
     *
     * @return the line ending string
     */
    public String str() {
        return switch (this) {
            case LF -> "\n";
            case CR -> "\r";
            case CRLF -> "\r\n";
        };
    }

    /**
     * Estimates the RowEnding symbols from the given number of symbols count.
     *
     * @param crCount the count of carriage return
     * @param lfCount the count of line feed
     * @return the estimated RowEnding
     */
    public static RowEnding estimate(int crCount, int lfCount) {
        return (crCount == 0 && lfCount == 0)
            ? RowEnding.platform
            : (crCount == lfCount)
            ? RowEnding.CRLF
            : (lfCount == 0)
            ? RowEnding.CR
            : RowEnding.LF;
    }

    /**
     * Get the platform row separator.
     *
     * @return the platform row separator
     */
    private static RowEnding platform() {
        return switch (System.lineSeparator()) {
            case "\r" -> CR;
            case "\r\n" -> CRLF;
            default -> LF;
        };
    }

    /**
     * Unify line separators to LF line breaks.
     *
     * @param text the text to be converted
     * @return the converted text
     */
    static CharSequence unifyLf(CharSequence text) {
        int prev = 0;
        StringBuilder sb = null;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\r') {
                if (sb == null) sb = new StringBuilder();
                sb.append(text.subSequence(prev, i));
                sb.append('\n');
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                prev = i + 1;
            }
        }
        if (sb != null) {
            sb.append(text.subSequence(prev, text.length()));
            return sb;
        } else {
            return text;
        }
    }

    /**
     * Unify line separators to CR line breaks.
     *
     * @param text the text to be converted
     * @return the converted text
     */
    static CharSequence unifyCr(CharSequence text) {
        int prev = 0;
        StringBuilder sb = null;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                if (sb == null) sb = new StringBuilder();
                sb.append(text.subSequence(prev, i));
                sb.append('\r');
                i++;
                prev = i + 1;
            } else if (ch == '\n') {
                if (sb == null) sb = new StringBuilder();
                sb.append(text.subSequence(prev, i));
                sb.append('\r');
                prev = i + 1;
            }
        }
        if (sb != null) {
            sb.append(text.subSequence(prev, text.length()));
            return sb;
        } else {
            return text;
        }
    }

    /**
     * Unify line separators to CR LF line breaks.
     *
     * @param text the text to be converted
     * @return the converted text
     */
    static CharSequence unifyCrLf(CharSequence text) {
        int prev = 0;
        StringBuilder sb = null;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                if (sb == null) sb = new StringBuilder();
                sb.append(text.subSequence(prev, i));
                sb.append("\r\n");
                prev = i + 1;
            } else if (ch == '\r') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                } else {
                    if (sb == null) sb = new StringBuilder();
                    sb.append(text.subSequence(prev, i));
                    sb.append("\r\n");
                    prev = i + 1;
                }
            }
        }
        if (sb != null) {
            sb.append(text.subSequence(prev, text.length()));
            return sb;
        } else {
            return text;
        }
    }

}
