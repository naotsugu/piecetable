package com.mammb.code.editor;

import java.util.ArrayList;
import java.util.List;

public class Strings {

    public static char CR = '\r';
    public static char LF = '\n';


    public static boolean hasLF(String str) {
        return str.indexOf(LF) > 0;
    }

    public static boolean hasMultiLF(String str) {
        int i = str.indexOf(LF);
        if (++i > 0 && i < str.length()) {
            return str.indexOf(LF, i) > 0;
        }
        return false;
    }


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

    public static int countLf(String str) {
        return (int) str.chars().filter(c -> c == LF).count();
    }

}
