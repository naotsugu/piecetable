package com.mammb.code.editor;

public interface EditListener {
    void preEdit(int lineNumber, int index, int length);
    void postEdit();
}
