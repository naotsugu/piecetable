/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.model;

/**
 * Coloring.
 * @author Naotsugu Kobayashi
 */
public interface Coloring {

    Coloring DarkOrange = new ColoringRec("#eb8a3a", 1.0);
    Coloring DarkGreen = new ColoringRec("#629755", 1.0);
    Coloring DarkGray = new ColoringRec("#808080", 1.0);
    Coloring DarkYellow = new ColoringRec("#d7d02f", 1.0);
    Coloring DarkSkyBlue = new ColoringRec("#78aed7", 1.0);

    String colorString();

    double opacity();

    record ColoringRec(String colorString, double opacity) implements Coloring { }

}
