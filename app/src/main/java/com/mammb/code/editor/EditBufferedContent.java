package com.mammb.code.editor;

import java.nio.file.Path;

public class EditBufferedContent implements Content {

    private final Content peer;
    private Edit edit;


    public EditBufferedContent(Content peer) {
        this.peer = peer;
        this.edit = new NeutralEdit(peer);
    }


    @Override
    public void insert(int pos, String cs) {
        edit = edit.insert(pos, cs);
    }

    @Override
    public void delete(int pos, int len) {
        edit = edit.delete(pos, len);
    }

    @Override
    public int length() {
        if (edit instanceof InsertEdit) {
            return peer.length() + edit.len();
        }
        if (edit instanceof DeleteEdit) {
            return peer.length() - edit.len();
        }
        return peer.length();
    }
    @Override
    public Path path() {
        return peer.path();
    }
    @Override
    public void write(Path path) {
        flush();
        peer.write(path);
    }

    @Override
    public void open(Path path) {
        flush();
        peer.open(path);
    }

    @Override
    public int codePointAt(int pos) {
        if (pos >= edit.pos()) {
            flush();
        }
        return peer.codePointAt(pos);
    }

    @Override
    public String substring(int start, int end) {
        if (end >= edit.pos()) {
            flush();
        }
        return peer.substring(start, end);
    }

    @Override
    public String untilEol(int pos) {
        if (pos >= edit.pos()) {
            flush();
        }
        return peer.untilEol(pos);
    }

    @Override
    public String untilSol(int pos) {
        flush();
        return peer.untilSol(pos);
    }

    @Override
    public int[] undo() {
        flush();
        return peer.undo();
    }

    @Override
    public int[] redo() {
        flush();
        return peer.redo();
    }

    void flush() {
        edit = edit.flush();
    }


    Edit getEdit() {
        return edit;
    }

    interface Edit {
        Edit insert(int pos, String cs);
        Edit delete(int pos, int len);
        Edit flush();
        default int pos() { return -1;}
        default int len() { return 0;}
    }


    record NeutralEdit(Content content) implements Edit {

        @Override
        public Edit insert(int pos, String cs) { return new InsertEdit(content, System.currentTimeMillis(), pos, cs); }
        @Override
        public Edit delete(int pos, int len) { return new DeleteEdit(content, System.currentTimeMillis(), pos, len); }
        @Override
        public Edit flush() { return this; }

    }

    record InsertEdit(Content content, long occurredOn, int pos, String str) implements Edit {

        @Override
        public Edit insert(int nextPos, String nextStr) {
            if ((System.currentTimeMillis() - occurredOn) < 2500 && (pos + str.length()) == nextPos) {
                return new InsertEdit(content, System.currentTimeMillis(), pos, str + nextStr);
            } else {
                content.insert(pos, str);
                return new InsertEdit(content, System.currentTimeMillis(), nextPos, nextStr);
            }
        }

        @Override
        public Edit delete(int nextPos, int nextLen) {
            content.insert(pos, str);
            return new DeleteEdit(content, System.currentTimeMillis(), nextPos, nextLen);
        }

        @Override
        public Edit flush() {
            content.insert(pos, str);
            return new NeutralEdit(content);
        }

        @Override
        public int pos() { return pos; }
        @Override
        public int len() { return str.length(); }

    }


    record DeleteEdit(Content content, long occurredOn, int pos, int len) implements Edit {

        @Override
        public Edit insert(int nextPos, String nextStr) {
            content.delete(pos, len);
            return new InsertEdit(content, System.currentTimeMillis(), nextPos, nextStr);
        }

        @Override
        public Edit delete(int nextPos, int nextLen) {
            if ((System.currentTimeMillis() - occurredOn) < 2500 &&
                (pos == nextPos || nextPos + nextLen == pos)) {
                return new DeleteEdit(content, System.currentTimeMillis(), nextPos, len + nextLen);
            } else {
                content.delete(pos, len);
                return new DeleteEdit(content, System.currentTimeMillis(), nextPos, nextLen);
            }
        }

        @Override
        public Edit flush() {
            content.delete(pos, len);
            return new NeutralEdit(content);
        }

        @Override
        public int pos() { return pos; }
        @Override
        public int len() { return len;}
    }

}
