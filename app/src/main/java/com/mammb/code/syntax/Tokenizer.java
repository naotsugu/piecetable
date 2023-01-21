package com.mammb.code.syntax;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Tokenizer {

    private final Queue<Token> tokenPool = new ConcurrentLinkedQueue<>();

    /** input string. */
    private String input;

    /** current position in input (points to current char). */
    private int position;

    /** current reading position in input (after current char). */
    private int readPosition;


    public Tokenizer init(String input) {
        this.input = input;
        this.position = 0;
        this.readPosition = 0;
        return this;
    }


    public abstract Token next();


    /**
     * Get the next character.
     * @return the next character
     */
    public char readChar() {
        char ch = peekChar();
        position = readPosition;
        readPosition += 1;
        return ch;
    }


    /**
     * Get the previous character.
     * @return the previous character
     */
    public char readAgain() {
        return input.charAt(position);
    }


    /**
     * Get the string.
     * from:position toExclusive:position + n
     * @param n length
     * @return read string
     */
    public String read(int n) {
        String str = input.substring(position, position + n);
        position += (n - 1);
        readPosition = position + 1;
        return str;
    }


    public int consume(int n) {
        int prev = position;
        position += (n - 1);
        readPosition = position + 1;
        return prev;
    }


    public char peekChar() { return peekChar(0); }
    public char peekChar(int n) {
        return (readPosition + n >= input.length()) ? 0 : input.charAt(readPosition + n);
    }

    public String input() { return input; }
    public int position() { return position; }
    public int readPosition() { return readPosition; }

    public Token token() {
        Token token = tokenPool.poll();
        return (token == null) ? new Token() : token;
    }

    public void back(Token token) {
        tokenPool.add(token);
    }

}
