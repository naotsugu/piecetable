/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.core.syntax;

import com.mammb.code.editor.core.text.Style.StyleSpan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The python syntax.
 * @author Naotsugu Kobayashi
 */
public class PythonSyntax implements Syntax {

    private final Trie keywords = Trie.of("""
        False,await,else,import,pass,None,break,except,in,raise,True,class,finally,is,return,
        and,continue,for,lambda,try,as,def,from,nonlocal,while,assert,del,global,not,with,
        async,elif,if,or,yield
        match, case
        """);

    @Override
    public String name() {
        return "python";
    }

    @Override
    public List<StyleSpan> apply(int row, String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        var spans = new ArrayList<StyleSpan>();
        var source = LexerSource.of(row, text);
        while (source.hasNext()) {
            var peek = source.peek();
            char ch = peek.ch();
            if (ch == '#') {
                var s = source.nextRemaining();
                var span = new StyleSpan(Palette.gray, s.index(), s.length());
                spans.add(span);
            } else if (isIdentifierStart(ch)) {
                var s = source.nextIdentifierPart();
                if (keywords.match(s.string())) {
                    var span = new StyleSpan(Palette.darkOrange, s.index(), s.length());
                    spans.add(span);
                }
            }
        }
        source.commitPeek();
        return spans;
    }

    /**
     * Determines if the character (Unicode code point) is permissible as the first character in an identifier.
     * <p>
     *   id_start ::= <all characters in general categories Lu, Ll, Lt, Lm, Lo, Nl, the underscore, and characters with the Other_ID_Start property>
     * </p>
     * @param cp the character (Unicode code point) to be tested
     * @return {@code true}, if the character may start an identifier
     */
    public static boolean isIdentifierStart(int cp) {
        int type = Character.getType(cp);
        return Character.isUnicodeIdentifierStart(cp)
                || type == Character.UPPERCASE_LETTER // Lu
                || type == Character.LOWERCASE_LETTER // Ll
                || type == Character.TITLECASE_LETTER // Lt
                || type == Character.MODIFIER_LETTER  // Lm
                || type == Character.OTHER_LETTER     // Lo
                || type == Character.LETTER_NUMBER    // Nl
                || cp == '_';
    }

}
