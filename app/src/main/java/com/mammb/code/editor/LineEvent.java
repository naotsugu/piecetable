package com.mammb.code.editor;

public record LineEvent(boolean isAdded, int line, int lineLength) {
    public boolean isRemoved() { return !isAdded; }

    public static LineEvent added(int line, int lineLength) {
        return new LineEvent(true, line, lineLength);
    }
    public static LineEvent removed(int line, int lineLength) {
        return new LineEvent(false, line, lineLength);
    }

}
