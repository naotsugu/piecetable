package com.mammb.code.editor2.model;

import java.util.function.Predicate;

public interface Content extends EventListener<Edit> {

    byte[] bytes(int startPos, Predicate<byte[]> until);

    byte[] bytesBefore(int startPos, Predicate<byte[]> until);

    ViewPoint createViewPoint();
}
