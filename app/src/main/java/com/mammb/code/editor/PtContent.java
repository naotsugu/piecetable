package com.mammb.code.editor;

import com.mammb.code.piecetable.PieceTable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class PtContent implements Content {

    private static final int bufferChars = 128;

    private PieceTable pt;

    private byte[] readBuffer;
    private int bufferHeadPos = -1;
    private int bufferTailPos = -1;


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
        if (pos < bufferTailPos) clearBuffer();
    }

    @Override
    public void delete(int pos, int len) {
        pt.delete(pos, len);
        if (pos < bufferTailPos) clearBuffer();
    }

    @Override
    public void write(Path path) {
        pt.write(path);
    }

    @Override
    public void open(Path path) {
        pt = PieceTable.of(path);
        clearBuffer();
    }


    @Override
    public String untilEol(int pos) {

        var list = new ArrayList<byte[]>();

        label:for (int startIndex = asBufferIndex(pos);;startIndex = asBufferIndex(bufferTailPos)) {

            if (startIndex < 0) break;

            for (int i = startIndex; i < readBuffer.length; i++) {
                if (readBuffer[i] == '\n') {
                    list.add(Arrays.copyOfRange(readBuffer, startIndex, i + 1));
                    break label;
                }
            }
            list.add(Arrays.copyOfRange(readBuffer, startIndex, readBuffer.length));
        }

        return toString(list);
    }

    @Override
    public String untilSol(int pos) {

        var list = new ArrayList<byte[]>();
        Boolean skipFirst = null;

        label:for (int startIndex = asBufferIndex(pos);;startIndex = readBuffer.length - 1) {

            if (startIndex < 0) break;

            for (int i = startIndex; i >= 0; i--) {
                skipFirst = (skipFirst == null && readBuffer[startIndex] == '\n');
                if (!skipFirst && readBuffer[i] == '\n') {
                    list.add(0, Arrays.copyOfRange(readBuffer, i + 1, startIndex + 1));
                    break label;
                }
                skipFirst = false;
            }
            list.add(0, Arrays.copyOfRange(readBuffer, 0, startIndex + 1));
            fillBuffer(Math.max(bufferHeadPos - bufferChars, 0));
        }
        return toString(list);
    }


    /**
     * Converts content position to buffer index.
     * @param pos content position
     * @return buffer index
     */
    private int asBufferIndex(int pos) {
        if (pos < 0 || pos > pt.length()) {
            throw new IllegalArgumentException("pos " + pos);
        }
        if (bufferHeadPos > pos || pos >= bufferTailPos) {
            fillBuffer(pos);
        }
        int count = pos - bufferHeadPos;
        if (count == 0) {
            return 0;
        }
        for (int i = 0; i < readBuffer.length; i++) {
            if ((readBuffer[i] & 0xC0) == 0x80) {
                continue;
            }
            if ((count--) == 0) {
                return i;
            }
        }
        return -1;
    }


    private void fillBuffer(int pos) {
        bufferHeadPos = Math.max(pos, 0);
        bufferTailPos = Math.min(pt.length(), pos + bufferChars);
        readBuffer = pt.bytes(bufferHeadPos, bufferTailPos);
    }


    private void clearBuffer() {
        bufferHeadPos = -1;
        bufferTailPos = -1;
        readBuffer = null;
    }

    private String toString(ArrayList<byte[]> list) {
        var byteArray = new ByteArrayOutputStream(bufferChars * list.size());
        for (byte[] bytes : list) {
            byteArray.write(bytes, 0, bytes.length);
        }
        return byteArray.toString(StandardCharsets.UTF_8);
    }

}
