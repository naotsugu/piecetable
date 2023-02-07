package com.mammb.code.editor3.model;

public class Token {

    private String name;
    private int position;
    private int length;

    private boolean adjoining(Token other) {
        return position + length == other.position;
    }

    public Token marge(Token other) {
        if (!adjoining(other)) {
            throw new IllegalArgumentException(other.toString());
        }
        this.length += other.length;
        return this;
    }

    public String name() { return name; }
    public int position() { return position; }
    public int length() { return length; }

}
