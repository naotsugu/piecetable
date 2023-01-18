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

    public static boolean hasLF(String str) {
        return str.indexOf(LF) > 0;
    }


    public static int countToken(char ch, String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ch) continue;
            if (c == ' ') return i;
            else return 0;
        }
        return 0;
    }
}
