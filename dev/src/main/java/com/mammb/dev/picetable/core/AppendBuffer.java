package com.mammb.dev.picetable.core;

public interface AppendBuffer extends Buffer {
    void append(byte[] bytes);
    void clear();

    static AppendBuffer of() {
        return new AppendBuffer() {
            private final ByteArray elements;
            { elements = ByteArray.of(); }

            @Override
            public void append(byte[] bytes) {
                elements.add(bytes);
            }

            @Override
            public void clear() {
                elements.clear();
            }

            @Override
            public byte get(long index) {
                return elements.get(Math.toIntExact(index));
            }

            @Override
            public byte[] bytes(long rawStart, long rawEnd) {
                return elements.get(Math.toIntExact(rawStart), Math.toIntExact(rawEnd));
            }

            @Override
            public long length() {
                return elements.length();
            }
        };
    }

}

