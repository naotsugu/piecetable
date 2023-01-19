package com.mammb.code.syntax;

abstract class Tokenizer {

    /** input string. */
    private final String input;

    /** current position in input (points to current char). */
    private int position;

    /** current reading position in input (after current char). */
    private int readPosition;

    public Tokenizer(String input) {
        this.input = input;
        this.position = 0;
    }

    abstract Token next();


    /**
     * Get the next character skipping a space.
     * @return the next character
     */
    char readNextChar() {
        char ch = readChar();
        while (isWhitespace(ch)) {
            ch = readChar();
        }
        return ch;
    }


    /**
     * Get the next character.
     * @return the next character
     */
    char readChar() {
        char ch = peekChar();
        position = readPosition;
        readPosition += 1;
        return ch;
    }


    String read(int n) {
        String str = input.substring(position, position + n);
        position += (n - 1);
        readPosition = position + 1;
        return str;
    }


    boolean nextIf(char c) {
        char next = peekChar();
        if (next == c) {
            readChar();
            return true;
        }
        return false;
    }

    char peekChar() {
        return (readPosition >= input.length()) ? 0 : input.charAt(readPosition);
    }
    char peekChar(int n) {
        return (readPosition + n >= input.length()) ? 0 : input.charAt(readPosition + n);
    }

    public int position() { return position; }
    public int readPosition() { return readPosition; }

    boolean isLetter(char c) { return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_'; }
    boolean isDigit(char c) { return '0' <= c && c <= '9'; }
    boolean isWhitespace(char c) { return c == ' ' || c == '\t' || c == '\n' || c == '\r'; }

}
