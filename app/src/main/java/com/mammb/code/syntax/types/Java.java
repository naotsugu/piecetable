package com.mammb.code.syntax.types;

import com.mammb.code.editor.Colors;
import com.mammb.code.syntax.Highlighter;
import com.mammb.code.syntax.LinePoint;
import com.mammb.code.syntax.Scope;
import com.mammb.code.syntax.ScopeMark;
import com.mammb.code.syntax.Token;
import com.mammb.code.syntax.Tokenizer;
import com.mammb.code.trie.Trie;
import javafx.scene.paint.Paint;
import java.util.stream.Stream;

public class Java implements Highlighter {

    private static final Trie keywords = keywordTrie();
    private final Tokenizer tokenizer = new JavaTokenizer();
    private final Scope scope = new Scope();

    @Override
    public Tokenizer tokenizer() { return tokenizer; }

    @Override
    public boolean invalidate(int line, int length) {
        int before = scope.size();
        scope.removeCeiling(line);
        return before != scope.size();
    }

    @Override
    public boolean blockEdgeContains(int line, int length) {
        return scope.containsEdge(line, length);
    }


    @Override
    public Paint colorOf(Token token) {
        return switch (token.name()) {
            case "lineComment" -> Colors.lineCommentColor;
            case "keyword" -> Colors.kwColor;
            case "string", "char", "textBlock" -> Colors.strLiteralColor;
            case "number" -> Colors.numberLiteralColor;
            case "blockComment" -> Colors.blockCommentColor;
            case "annotation" -> Colors.yellowColor;
            case "method" -> Colors.functionColor;
            default  -> Colors.fgColor;
        };
    }

    @Override
    public Token decorate(int line, Token token) {

        LinePoint point = new LinePoint(line, token.position());
        LinePoint hwPoint = scope.highWaterPosition();

        if (hwPoint == null || point.compareTo(hwPoint) > 0) {
            if (token.name().equals("/*")) {
                scope.push(ScopeMark.startOf("blockComment", point, 2));
            } else if (token.name().equals("*/")) {
                scope.push(ScopeMark.endOf("blockComment", point, 2));
            } else if (token.name().equals("\"\"\"")) {
                scope.push(ScopeMark.of("textBlock", point, 3));
            }
        }

        if (scope.within(point, "blockComment")) {
            token.with("blockComment", token.position(), token.length());
        } else if (scope.within(point, "textBlock")) {
            token.with("textBlock", token.position(), token.length());
        }
        return token;

    }


    @Override
    public String inspect() {
        return scope.inspect();
    }



    static class JavaTokenizer extends Tokenizer {

        @Override
        public Token next() {
            char ch = readChar();
            int pos = position();
            return switch (ch) {
                case ' ', '\t', '\n', '\r' -> token().with("sp", pos, 1);
                case '0','1','2','3','4','5','6','7','8','9','-' -> Helper.readNumber(this, "number");
                case '"'  -> readText();
                case '\'' -> Helper.readChar(this,"char", '\'');
                case '@'  -> readIdentifier("annotation");
                case  0   -> token().empty(pos);
                case '/'  -> {
                    if (peekChar(0) == '*') yield readOpenBlockComment();
                    else yield Helper.stdLineComment(this,"lineComment", '/');
                }
                case '*'  -> readCloseBlockComment();
                default   -> {
                    if (Character.isJavaIdentifierStart(ch)) {
                        yield readIdentifier();
                    } else {
                        yield token().itself(Helper.str(ch), pos);
                    }
                }
            };
        }

        private Token readIdentifier() {
            return readIdentifier("");
        }

        private Token readIdentifier(String name) {
            int pos = position();
            int n = 0;
            for (;;) {
                char ch = peekChar(n++);
                if (!Character.isJavaIdentifierPart(ch)) {
                    String str = read(n);
                    if (keywords.match(str)) {
                        return token().with("keyword", pos, n);
                    }
                    return token().with(name.isEmpty() ? str : name, pos, str.length());
                }
            }
        }

        private Token readOpenBlockComment() {
            if (peekChar(0) == '*') {
                return token().with("/*", consume(2), 2);
            } else {
                return token().itself("/", position());
            }
        }

        private Token readCloseBlockComment() {
            if (peekChar(0) == '/') {
                return token().with("*/", consume(2), 2);
            } else {
                return token().itself("*", position());
            }
        }

        private Token readText() {
            if (peekChar(0) == '"' && peekChar(1) == '"') {
                return token().with("\"\"\"", consume(3), 3);
            }
            return Helper.readString(this, "string", '"');
        }

    }

    private static Trie keywordTrie() {
        Trie trie = new Trie();
        Stream.of("""
        abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,
        this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,
        return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,
        strictfp,volatile,const,float,native,super,while,var,record,sealed,with,yield,to,transitive,uses"""
        .split("[,\\s]")).forEach(trie::put);
        return trie;
    }
}
