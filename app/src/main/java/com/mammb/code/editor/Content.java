package com.mammb.code.editor;

import java.nio.file.Path;

public interface Content {
    int length();
    void insert(int pos, String cs);
    void delete(int pos, int len);
    Path path();
    void write(Path path);
    void open(Path path);
    int codePointAt(int pos);
    String substring(int start, int end);
    String untilEol(int pos);
    String untilSol(int pos);
    int[] undo();
    int[] redo();

    default void write() {
        if (path() == null) {
            throw new IllegalStateException("path is null");
        }
        write(path());
    }

    default String lineAt(int pos) {
        String right = untilEol(pos);
        return untilSol(pos) + (right.length() > 0 ? right.substring(1) : "");
    }

}
