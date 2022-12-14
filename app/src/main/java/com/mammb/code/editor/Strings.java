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
     * aa\nbb -> aa\n
     *           bb
     * </pre>
     * @param str
     * @return
     */
    public static List<String> splitLine(String str) {
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
        return list;
    }

}
