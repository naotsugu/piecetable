package com.mammb.code.editor2.model;

import com.mammb.code.piecetable.PieceTable;

import java.nio.file.Path;
import java.util.function.Predicate;

public class BasicContent implements Content {

    /** The piece table. */
    private PieceTable pt;

    /** The content path. */
    private Path path;

    /**
     * Constructor.
     */
    public BasicContent() {
        this.pt = PieceTable.of("");
    }

    @Override
    public byte[] bytes(int startPos, Predicate<byte[]> until) {
        return pt.bytes(startPos, until);
    }

    @Override
    public byte[] bytesBefore(int startPos, Predicate<byte[]> until) {
        return pt.bytesBefore(startPos, until);
    }

    @Override
    public ViewPoint createViewPoint() {
        return new ViewPoint(this);
    }

    @Override
    public void handle(Edit event) {

    }

}
