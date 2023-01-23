package com.mammb.code.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Strings {

    public static char CR = '\r';
    public static char LF = '\n';


    /**
     * <pre>
     * |a|a|\n|b|b| -> |a|a|\n|
     *                 |b|b|
     * </pre>
     * <pre>
     * |a|a|\n| -> |a|a|\n|
     *             ||
     * </pre>
     * <pre>
     * |\n|b|b| -> |\n|
     *             |b|b|
     * </pre>
     * @param str
     * @return
     */
    public static String[] splitLine(String str) {
        String[] lines = new String[countLf(str) + 1];
        int line = 0;
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == LF) {
                lines[line++] = str.substring(n, i + 1);
                n = i + 1;
            } else if (i == str.length() - 1) {
                lines[line++] = str.substring(n);
            }
        }
        if (line < lines.length) {
            lines[line] = "";
        }
        return lines;
    }


    /**
     * <pre>
     * |a|a|\n|b|b| -> |a|a|\n|
     *                 |b|b|
     * </pre>
     * <pre>
     * |a|a|\n| -> |a|a|\n|
     * </pre>
     * @param str
     * @return
     */
    public static String[] splitLf(String str) {
        List<String> list = new ArrayList<>();
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == LF) {
                list.add(str.substring(n, i + 1));
                n = i + 1;
            }
        }
        if (n < str.length()) {
            list.add(str.substring(n));
        }
        return list.toArray(new String[0]);
    }

    public static int countLf(String str) {
        return (int) str.chars().filter(c -> c == LF).count();
    }

    public static int countLf(byte[] bytes) {
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == LF) count++;
        }
        return count;
    }


    public static int lastLineLength(String str) {
        int index = str.lastIndexOf('\n');
        if (index > -1 && index + 1 < str.length()) {
            return str.substring(index + 1).length();
        }
        return 0;
    }


    /**
     * Returns the number of Unicode code points in the specified text.
     * @param str the specified text
     * @return the number of Unicode code points
     */
    public static int codePointCount(String str) {
        return (str == null) ? 0 : str.codePointCount(0, str.length());
    }

    /**
     * Returns the number of Unicode code points in the specified text range of this String.
     * @param str the specified text
     * @param beginIndex the index to the first char of the text range
     * @return the number of Unicode code points
     */
    public static int codePointCount(String str, int beginIndex) {
        return (str == null) ? 0 : str.codePointCount(beginIndex, str.length());
    }

    /**
     * Returns the number of Unicode code points in the specified text range of this String.
     * @param str the specified text
     * @param beginIndex the index to the first char of the text range
     * @param endIndex the index after the last char of the text range
     * @return the number of Unicode code points
     */
    public static int codePointCount(String str, int beginIndex, int endIndex) {
        return (str == null) ? 0 : str.codePointCount(beginIndex, endIndex);
    }


    public static int widthIndex(String str, int asciiIndex) {
        int[] ints = str.codePoints().map(cp -> {
            if (cp < 1_024) {
                // latin
                return 1;
            } else if ((65_377 <= cp && cp <= 65_500) || (65_512 <= cp && cp <= 65_518) ) {
                // FF00～FFEF The Unicode Standard Half-width and Full-width Forms
                // Half-width
                return 1;
            } else {
                // maybe not Half-width
                return Math.max(Character.charCount(cp), 2);
            }
        }).toArray();
        int count = 0;
        for (int i = 0; i < ints.length; i++) {
            count += ints[i];
            if (count >= asciiIndex) return i;
        }
        return str.length() - 1;
    }

}
