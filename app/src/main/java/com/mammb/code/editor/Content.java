package com.mammb.code.editor;

import java.nio.file.Path;

public interface Content {
    int length();
    void insert(int pos, String cs);
    void delete(int pos, int len);
    void write(Path path);
    void open(Path path);
    String untilEol(int pos);
    String untilSol(int pos);
}
