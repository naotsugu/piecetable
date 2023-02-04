package com.mammb.code.editor2.model;

/**
 * EventListener.
 * @author Naotsugu Kobayashi
 */
public interface EventListener<T> {
    void handle(T event);
}
