package com.mammb.code.syntax.types;

import com.mammb.code.syntax.Highlighter;
import com.mammb.code.syntax.Token;
import com.mammb.code.syntax.Tokenizer;

public class PassThrough implements Highlighter {

    private final Tokenizer tokenizer = new PassThroughTokenizer();

    @Override
    public Tokenizer tokenizer() {
        return tokenizer;
    }

    static class PassThroughTokenizer extends Tokenizer {
        private int length;

        @Override
        public Tokenizer init(String input) {
            this.length = input.length();
            return super.init(input);
        }

        @Override
        public Token next() {
            int len = length;
            length = 0;
            return token().with("pt", 0, len);
        }
    }
}

