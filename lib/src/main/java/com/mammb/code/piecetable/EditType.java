package com.mammb.code.piecetable;

public enum EditType {

    INS, DEL, NIL;

    public boolean isInsert() { return this == INS; }
    public boolean isDelete() { return this == DEL; }
    public boolean isNil()    { return this == NIL; }

}
