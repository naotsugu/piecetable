package com.mammb.code.editor3.model;

import com.mammb.code.editor3.model.specifics.NowrapScrollHandler;
import com.mammb.code.editor3.model.specifics.WrapScrollHandler;

public interface ScrollHandler {

    /**
     * Scroll next.
     */
    void next();

    /**
     * Scroll prev.
     */
    void prev();

    /**
     * Create the scroll handler.
     * @param textWrap text wrap?
     * @return the scroll handler
     */
    static ScrollHandler of(boolean textWrap) {
        return textWrap
            ? new WrapScrollHandler()
            : new NowrapScrollHandler();
    }

}
