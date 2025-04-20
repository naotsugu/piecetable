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
 * The result of charset match.
 * @author Naotsugu Kobayashi
 */
public class CharsetMatchResult implements CharsetMatch.Result {

    /** The charset. */
    private final Charset charset;
    /** The confidence value. */
    private int confidence = 50;
    /** The count of miss. */
    private int miss = 0;
    /** The vague. */
    private boolean vague = true;

    CharsetMatchResult(Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    /**
     * Increases the confidence value.
     */
    void increasesConfidence() {
        if (confidence < 100) confidence++;
        vague = false;
    }

    /**
     * Decreases the confidence value.
     */
    void decreasesConfidence() {
        if (confidence > 0) confidence--;
        miss++;
        vague = false;
    }

    /**
     * Increases the confidence by the specified value
     * @param n the value to be increased
     */
    void increasesConfidence(int n) {
        confidence += n;
        confidence = Math.clamp(confidence, 0, 100);
        vague = false;
    }

    /**
     * Decreases the confidence by the specified value
     * @param n the value to be decreased
     */
    void decreasesConfidence(int n) {
        confidence -= n;
        confidence = Math.clamp(confidence, 0, 100);
        miss++;
        vague = false;
    }

    /**
     * Confirmed confidence value.
     */
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
