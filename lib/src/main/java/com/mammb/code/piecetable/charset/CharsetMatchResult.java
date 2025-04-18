/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.charset;

import com.mammb.code.piecetable.CharsetMatch;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * The charset match result.
 * @author Naotsugu Kobayashi
 */
public class CharsetMatchResult implements CharsetMatch.Result {

    private final Charset charset;
    private int confidence = 50;
    private int miss = 0;
    private boolean vague = true;

    CharsetMatchResult(Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    void increasesConfidence() {
        if (confidence < 100) confidence++;
        vague = false;
    }

    void decreasesConfidence() {
        if (confidence > 0) confidence--;
        miss++;
        vague = false;
    }

    void increasesConfidence(int n) {
        confidence += n;
        confidence = Math.clamp(confidence, 0, 100);
        vague = false;
    }

    void decreasesConfidence(int n) {
        confidence -= n;
        confidence = Math.clamp(confidence, 0, 100);
        miss++;
        vague = false;
    }

    void exact() {
        confidence = 100;
        vague = false;
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @Override
    public int confidence() {
        return confidence;
    }

    @Override
    public int miss() {
        return miss;
    }

    @Override
    public boolean isVague() {
        return vague;
    }

    @Override
    public String toString() {
        return "%s confidence:%d, miss:%d vague:%s".formatted(charset, confidence, miss, vague);
    }

}
