package com.mammb.code.editor2.model;

public interface Content extends EventListener<Edit> {
    ViewPoint createViewPoint();
}
