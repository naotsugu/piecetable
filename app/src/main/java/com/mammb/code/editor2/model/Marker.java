/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor2.model;

import java.util.StringJoiner;

/**
 * Marker.
 * @author Naotsugu Kobayashi
 */
public class Marker {

    private enum Type { START, END, NEUTRAL }

    private final String name;
    private final Type type;
    private final int size;
    private Marker pair;

    public Marker(String name, Type type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public static Marker of(String name, int size) {
        return new Marker(name, Type.NEUTRAL, size);
    }

    public static Marker startOf(String name, int size) {
        return new Marker(name, Type.START, size);
    }

    public static Marker endOf(String name, int size) {
        return new Marker(name, Type.END, size);
    }

    public void linkTo(Marker other) {
        this.pair = other;
        other.pair = this;
    }

    public void unlink() {
        if (pair != null) {
            pair.pair = null;
            pair = null;
        }
    }

    public boolean hasPair() { return pair != null; }
    public boolean isStart() { return type == Type.START; }
    public boolean isEnd() { return type == Type.END; }
    public boolean isNeutral() { return type == Type.NEUTRAL; }

    public String name() { return name; }
    public Marker pair() { return pair; }
    public int size() { return size; }

    @Override
    public String toString() {
        return new StringJoiner(", ", Marker.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("type=" + type)
            .add("size=" + size)
            .add("pair=" + pair.hashCode())
            .toString();
    }

}
