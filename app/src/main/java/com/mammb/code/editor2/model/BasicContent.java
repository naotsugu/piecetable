package com.mammb.code.editor2.model;

import com.mammb.code.piecetable.PieceTable;

import java.nio.file.Path;

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

    public ViewPoint createViewPoint() {
        return new ViewPoint(this);
    }

    @Override
    public void handle(Edit event) {

    }

}
