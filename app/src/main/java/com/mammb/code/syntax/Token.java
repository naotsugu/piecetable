package com.mammb.code.syntax;

public class Token {
    private String name;
    private int position;
    private int length;

    public Token with(String name, int position, int length) {
        this.name = name;
        this.position = position;
        this.length = length;
        return this;
    }

    public Token itself(String name, int position) {
        if (name.length() != 1) throw new IllegalArgumentException(name);
        return with(name, position, 1);
    }

    public Token empty(int position) {
        return with("", position, 0);
    }

    public boolean adjoining(Token other) {
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
