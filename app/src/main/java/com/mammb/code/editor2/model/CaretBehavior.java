package com.mammb.code.editor2.model;

public interface CaretBehavior {
    void next();
    void prev();
    void NextRow();
    void prevRow();
    void home();
    void end();
}
