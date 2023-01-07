package com.mammb.code.piecetable;

public enum EditType {

    INS, DEL, NIL;

    public EditType flip() {
        return switch (this) {
            case DEL -> INS;
            case INS -> DEL;
            case NIL -> NIL;
        };
    }

    public boolean isInsert() {
        return this == INS;
    }

    public boolean isDelete() {
        return this == DEL;
    }

    public boolean isNil() {
        return this == NIL;
    }

}
