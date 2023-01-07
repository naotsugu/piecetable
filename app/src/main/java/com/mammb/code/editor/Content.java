package com.mammb.code.editor;

import com.mammb.code.piecetable.Edited;

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
    Edited undo();
    int undoSize();
    Edited redo();


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


    default int lineCount() {
        int count = 0;
        for (int i = 0; i < length();) {
            int end = Math.min(i + 512, length());
            count += substring(i, end).chars().filter(c -> c == '\n').count();
            i = end;
        }
        return count;
    }

}
