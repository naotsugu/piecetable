package com.mammb.code.editor2.model;

public interface Edit {

    long occurredOn();
    int pos();
    CharSequence cs();
    Edit posWith(int pos);

    record InsertEdit(int pos, CharSequence cs, long occurredOn) implements Edit {
        public Edit posWith(int pos) {
            return new InsertEdit(pos, cs, occurredOn);
        }
    }

    record DeleteEdit(int pos, CharSequence cs, long occurredOn) implements Edit {
        public Edit posWith(int p) {
            return new DeleteEdit(p, cs, occurredOn);
        }
    }
}
