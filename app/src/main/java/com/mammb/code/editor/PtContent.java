package com.mammb.code.editor;

import com.mammb.code.piecetable.PieceTable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class PtContent implements Content {

    private static final int bufferChars = 256;

    private PieceTable pt;
    private Path path;

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
    public Path path() { return path; }
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
        this.pt.write(path);
        this.pt = PieceTable.of(path);
        this.path = path;
    }

    @Override
    public void open(Path path) {
        this.pt = PieceTable.of(path);
        this.path = path;
        clearBuffer();
    }

    @Override
    public int codePointAt(int pos) {
        // unbuffered
        // |A|B| -> code point of B
        return Character.codePointAt(pt.substring(pos, pos + 1), 0);
    }

    @Override
    public String substring(int start, int end) {
        // unbuffered
        return pt.substring(start, end);
    }

    @Override
    public String untilEol(int pos) {

        var list = new ArrayList<byte[]>();

        label:for (int startIndex = asBufferIndex(pos);;
                   startIndex = asBufferIndex(bufferTailPos)) {

            if (startIndex < 0) break;

            for (int i = startIndex; i < readBuffer.length; i++) {
                if (readBuffer[i] == '\n') {
                    list.add(Arrays.copyOfRange(readBuffer, startIndex, i + 1));
                    break label;
                }
            }
            list.add(Arrays.copyOfRange(readBuffer, startIndex, readBuffer.length));

            if (bufferTailPos >= pt.length()) {
                break;
            }
        }

        return toString(list);
    }

    @Override
    public String untilSol(int pos) {

        var list = new ArrayList<byte[]>();
        Boolean skipFirst = null;

        label:for (int startIndex = asBufferIndexBackward(pos);;
                   startIndex = asBufferIndexBackward(bufferHeadPos - 1)) {

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
            if (bufferHeadPos == 0) {
                break;
            }
        }
        return toString(list);
    }

    @Override
    public int[] undo() {
        int[] ret = pt.undo();
        if (ret.length > 1 && ret[0] < bufferTailPos) clearBuffer();
        return ret;
    }

    @Override
    public int undoSize() {
        return pt.undoSize();
    }

    @Override
    public int[] redo() {
        int[] ret = pt.redo();
        if (ret.length > 1 && ret[0] < bufferTailPos) clearBuffer();
        return ret;
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

        if (pos < bufferHeadPos || pos >= bufferTailPos) {
            //    + pos               + pos
            // |x|x|-|-|-|-|-|-|-|-|-|x|x|x|
            //      L bufferHeadPos   L bufferTailPos
            fillBuffer(pos);
        }

        return toIndex(pos);
    }


    private int asBufferIndexBackward(int endPos) {

        if (endPos < 0 || endPos > pt.length()) {
            throw new IllegalArgumentException("endPos " + endPos);
        }

        if (endPos < bufferHeadPos || endPos >= bufferTailPos) {
            fillBufferBackward(endPos);
        }

        return toIndex(endPos);
    }


    private void fillBuffer(int pos) {
        bufferHeadPos = Math.max(pos, 0);
        bufferTailPos = Math.min(pt.length(), pos + bufferChars);
        readBuffer = pt.bytes(bufferHeadPos, bufferTailPos);
    }


    private void fillBufferBackward(int endPos) {
        bufferHeadPos = Math.max(endPos + 1 - bufferChars, 0);
        bufferTailPos = Math.min(pt.length(), endPos + 1);
        readBuffer = pt.bytes(bufferHeadPos, bufferTailPos);
    }


    int toIndex(int pos) {

        if (pos < bufferHeadPos || pos > bufferTailPos) {
            throw new IllegalArgumentException("pos:%d, bufferHeadPos:%d, bufferTailPos:%d"
                .formatted(pos, bufferHeadPos, bufferTailPos));
        }

        //            + pos
        // |0|1|2|3|4|5|6|7|8|9|
        //      + bufferHeadPos
        //      |<-3->| <- char count
        int count = pos - bufferHeadPos;
        if (count == 0) {
            return 0;
        }

        for (int i = 0; i < readBuffer.length; i++) {
            // |x|.|.|x|.|x|.|.|x|.|x|
            //  +3    +2  +1    +0
            if ((readBuffer[i] & 0xC0) == 0x80) {
                continue;
            }
            if ((count--) == 0) {
                return i;
            }
        }
        return -1;
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
