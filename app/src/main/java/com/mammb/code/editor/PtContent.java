package com.mammb.code.editor;

import com.mammb.code.piecetable.PieceTable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class PtContent implements Content {

    private PieceTable pt;

    public PtContent(PieceTable pt) {
        this.pt = pt;
    }

    public PtContent() {
        this(PieceTable.of(""));
    }

    @Override
    public int length() {
        return pt.length();
    }

    @Override
    public void insert(int pos, String str) {
        pt.insert(pos, str);
    }

    @Override
    public void delete(int pos, int len) {
        pt.delete(pos, len);
    }

    @Override
    public void write(Path path) {
        pt.write(path);
    }

    @Override
    public void open(Path path) {
        pt = PieceTable.of(path);
    }

    @Override
    public String untilEol(int pos) {

        var list = new ArrayList<byte[]>();
        int bufferSize = 128;

        label:for (int i = pos; i < pt.length() + bufferSize; i += bufferSize) {
            byte[] bytes = pt.bytes(i, Math.min(pt.length(), i + bufferSize));
            for (int j = 0; j < bytes.length; j++) {
                if (bytes[j] == '\n') {
                    list.add(Arrays.copyOf(bytes, j + 1));
                    break label;
                }
            }
            list.add(bytes);
        }

        var byteArray = new ByteArrayOutputStream(bufferSize * list.size());
        for (byte[] bytes : list) {
            byteArray.write(bytes, 0, bytes.length);
        }
        return byteArray.toString(StandardCharsets.UTF_8);

    }



}
