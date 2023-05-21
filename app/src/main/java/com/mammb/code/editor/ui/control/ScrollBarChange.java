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
package com.mammb.code.editor.ui.control;

/**
 * ScrollBarChange.
 * @author Naotsugu Kobayashi
 */
public interface ScrollBarChange {

    /** axis. */
    enum Axis { HORIZONTAL, VERTICAL }

    /** type. */
    enum Type { THUMB, TRACK }


    /**
     * Get the axis.
     * @return the axis
     */
    Axis axis();


    /**
     * Get the type.
     * @return the type
     */
    Type type();


    /**
     * Get the distance.
     * @return the distance
     */
    double distance();


    /**
     * Creates new row thumb moved event.
     * @param distance the distance
     * @return ScrollBarChange
     */
    static ScrollBarChange rowThumbMovedOf(double distance) {
        return new ScrollBarChangeRecord(Axis.VERTICAL, Type.THUMB, distance);
    }


    /**
     * Creates new row truck clicked event.
     * @param distance the distance
     * @return ScrollBarChange
     */
    static ScrollBarChange rowTruckClickedOf(double distance) {
        return new ScrollBarChangeRecord(Axis.VERTICAL, Type.TRACK, distance);
    }


    /**
     * Creates new column thumb moved event.
     * @param distance the distance
     * @return ScrollBarChange
     */
    static ScrollBarChange colThumbMovedOf(double distance) {
        return new ScrollBarChangeRecord(Axis.HORIZONTAL, Type.THUMB, distance);
    }


    /**
     * Creates new column truck clicked event.
     * @param distance the distance
     * @return ScrollBarChange
     */
    static ScrollBarChange colTruckClickedOf(double distance) {
        return new ScrollBarChangeRecord(Axis.HORIZONTAL, Type.TRACK, distance);
    }


    /**
     * ScrollBarChangeRecord.
     * @param axis the axis
     * @param type the type
     * @param distance the distance
     */
    record ScrollBarChangeRecord(Axis axis, Type type, double distance) implements ScrollBarChange {}

}
