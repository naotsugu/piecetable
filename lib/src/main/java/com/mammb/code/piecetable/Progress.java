/*
 * Copyright 2023-2025 the original author or authors.
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
package com.mammb.code.piecetable;

/**
 * The progress.
 * @param <T> The type of partial result
 * @author Naotsugu Kobayashi
 */
public interface Progress<T> {

    /**
     * A value from Double.MIN_VALUE up to max.
     * If the value is greater than max, then it will be clamped at max.
     * If the value passed is negative, or Infinity, or NaN, then the resulting percentDone will be -1 (thus, indeterminate).
     * @return the up to max value
     */
    double workDone();

    /**
     * A value from Double.MIN_VALUE to Double.MAX_VALUE.
     * Infinity and NaN are treated as -1.
     * @return the max value
     */
    double max();

    /**
     * The partial result.
     * @return the partial result
     */
    T partial();

    /**
     * Create a next workDone progress.
     * @param workDelta the work delta
     * @param partial the partial result
     * @return a new {@link Progress}
     */
    default Progress<T> workDone(double workDelta, T partial) {
        return of(workDone() + workDelta, max(), partial);
    }

    /**
     * Create a new {@link Progress}.
     * @param workDone the up to max value
     * @param max the max value
     * @param partial the partial result
     * @return a new {@link Progress}
     * @param <T> the type of partial results
     */
    static <T> Progress<T> of(double workDone, double max, T partial) {
        record ProgressRecord<T>(double workDone, double max, T partial) implements Progress<T> { }
        return new ProgressRecord<>(workDone, max, partial);
    }

    /**
     * Create a new {@link Progress}.
     * @param workDone the up to max value
     * @param max the max value
     * @return a new {@link Progress}
     */
    static Progress<Void> of(double workDone, double max) {
        return of(workDone, max, null);
    }

    /**
     * Progress listener.
     * @param <T> the type of partial result
     */
    interface Listener<T> {
        /**
         * Accepts the progress.
         * @param progress the progress value
         * @return {@code true} to continue progress
         */
        boolean accept(Progress<T> progress);
    }

}
