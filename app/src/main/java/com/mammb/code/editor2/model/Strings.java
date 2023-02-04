package com.mammb.code.editor2.model;

/**
 * The string utilities.
 * @author Naotsugu Kobayashi
 */
public class Strings {

    /** carriage return. */
    public static char CR = '\r';
    /** line feed. */
    public static char LF = '\n';

    /**
     * Count the number of line feed in the specified char sequence.
     * @param cs the specified char sequence
     * @return the number of line feed
     */
    public static int countLf(CharSequence cs) {
        return (int) cs.chars().filter(c -> c == LF).count();
    }

}
