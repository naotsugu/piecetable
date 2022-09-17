package com.mammb.code.piecetable.array;

/**
 * Array utility.
 * @author Naotsugu Kobayashi
 */
public class ArraySupport {

    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    public static int newCapacity(int oldLength, int minGrowth, int prefGrowth) {
        int prefLength = oldLength + Math.max(minGrowth, prefGrowth);
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            int minLength = oldLength + minGrowth;
            if (minLength < 0) {
                throw new OutOfMemoryError(
                    "Required array length %d %d is too large".formatted(oldLength, minGrowth));
            }
            return Math.max(minLength, SOFT_MAX_ARRAY_LENGTH);
        }
    }

}
