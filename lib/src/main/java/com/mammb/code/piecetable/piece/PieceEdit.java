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
package com.mammb.code.piecetable.piece;

import com.mammb.code.piecetable.EditType;
import com.mammb.code.piecetable.Edited;
import com.mammb.code.piecetable.array.ByteArray;
import com.mammb.code.piecetable.buffer.Buffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Piece edit.
 * @param index The index of buffer
 * @param org The original pieces
 * @param mod The modified pieces
 * @param place
 */
public record PieceEdit(int index, Piece[] org, Piece[] mod, Place place) {

    public enum Place { HEAD, MID, TAIL }

    public PieceEdit {
        Objects.requireNonNull(org);
        Objects.requireNonNull(mod);
        Objects.requireNonNull(place);
    }

    public PieceEdit flip() {
        return new PieceEdit(index, mod, org, place);
    }

    public int totalOrgLength() {
        return Arrays.stream(org()).mapToInt(Piece::length).sum();
    }

    public int totalModLength() {
        return Arrays.stream(mod()).mapToInt(Piece::length).sum();
    }


    /**
     *
     * <pre>
     * -- just insert
     *   org     mod
     *          |+++|
     * </pre>
     * <pre>
     *  -- split insert
     *   org     mod
     *  |---|   |---|
     *          |+++|
     *          |---|
     * </pre>
     * <pre>
     *  -- just delete
     *   org     mod
     *  |---|
     * </pre>
     * <pre>
     *  -- head delete
     *   org     mod
     *  |---|   |+++|
     *  |+++|
     * </pre>
     * <pre>
     *  -- tail delete
     *   org     mod
     *  |+++|   |+++|
     *  |---|
     * </pre>
     * <pre>
     *  -- split delete
     *   org     mod
     *  |+++|   |+++|
     *  |---|
     *  |+++|   |+++|
     * </pre>
     * @param from
     * @return
     */
    public Edited asEdited(int from) {

        int totalOrgLength = totalOrgLength();
        int totalModLength = totalModLength();

        if (totalOrgLength < totalModLength) {
            int len = totalModLength - totalOrgLength;
            if (org.length == 0) {
                // just insert or revert just delete
                //  org[-]     mod[1]
                //             |++++|
                return new Edited(EditType.INS, from, len, mod[0].bytes().bytes());
            }
            if (org.length == 1 && mod.length == 3 && place == PieceEdit.Place.MID) {
                // split insert
                //  org[1]     mod[3]
                //  |....|     |....|
                //             |++++|
                //             |....|
                int offset = mod[0].length();
                from += offset;
                return new Edited(EditType.INS, from, len,
                    bytes(mod, offset, offset + len).get());
            }
            if (org.length == 1 && place == PieceEdit.Place.HEAD) {
                // revert head delete
                //  org[1]     mod[1..]
                //  |....|     |++++|
                //             |....|
                return new Edited(EditType.INS, from, len,
                    bytes(mod, 0, len).get());
            }
            if (org.length == 1 && place == PieceEdit.Place.TAIL) {
                // revert tail delete
                //  org[1]     mod[1..]
                //  |....|     |....|
                //             |++++|
                int offset = org[0].length();
                from += offset;
                return new Edited(EditType.INS, from, len,
                    bytes(mod, offset, offset + len).get());
            }
            if (org.length == 2 && place == PieceEdit.Place.MID) {
                // revert split delete
                //  org[2]     mod[1..]
                //  |....|     |....|
                //             |++++|
                //  |....|     |....|
                int offset = org[0].length();
                from += offset;
                return new Edited(EditType.INS, from, len,
                    bytes(mod, offset, offset + len).get());
            }
        }
        if (totalOrgLength > totalModLength) {
            int len = totalOrgLength - totalModLength;
            if (mod.length == 0) {
                // revert just insert or just delete
                //  org[1]     mod[1]
                //  |----|
                return new Edited(EditType.DEL, from, len, org[0].bytes().bytes());
            }
            if (org.length == 3 && mod.length == 1 && place == PieceEdit.Place.MID) {
                // revert split insert
                //  org[3]     mod[1]
                //  |....|     |....|
                //  |----|
                //  |....|
                int offset = org()[0].length();
                from += offset;
                return new Edited(EditType.DEL, from, len,
                    bytes(org, offset, offset + len).get());
            }
            if (mod.length == 1 && place == PieceEdit.Place.HEAD) {
                // head delete
                //  org[1..]   mod[1]
                //  |----|     |....|
                //  |....|
                return new Edited(EditType.DEL, from, len,
                    bytes(org, 0, len).get());
            }
            if (mod.length == 1 && place == PieceEdit.Place.TAIL) {
                // tail delete
                //  org[1..]   mod[1]
                //  |....|     |....|
                //  |----|
                int offset = mod[0].length();
                from += offset;
                return new Edited(EditType.DEL, from, len,
                    bytes(org, offset, offset + len).get());
            }
            if (mod.length == 2 && place == PieceEdit.Place.MID) {
                // split delete
                //  org[1..]   mod[2]
                //  |....|     |....|
                //  |----|
                //  |....|     |....|
                int offset = mod[0].length();
                from += offset;
                return new Edited(EditType.DEL, from, len,
                    bytes(org, offset, offset + len).get());
            }
        }
        return Edited.empty;
    }


    /**
     *
     * <pre>
     * |0|1|2|  len:3           |0|1|2|         startPos:4, endPos:14
     * |0|1|2|3|  len:4         |3|4|5|6|          |4|5|6|
     * |0|1|2|3|4|  len:5       |7|8|9|a|b|     |7|8|9|a|b|
     * |0|1|2|  len:3           |c|d|e|         |c|d|
     * </pre>
     *
     * @param pieces
     * @param startPos
     * @param endPos
     * @return
     */
    private static ByteArray bytes(Piece[] pieces, int startPos, int endPos) {
        ByteArray byteArray = ByteArray.of();
        int count = 0;
        for (int i = 0; i < pieces.length; i++) {
            Buffer buf = pieces[i].bytes();
            if (count + buf.length() > startPos) {
                byteArray.add(buf.subBuffer(
                    Math.max(startPos - count, 0),
                    Math.min(endPos - count, buf.length())
                ).bytes());
            }
            count += buf.length();
            if (count >= endPos) {
                break;
            }
        }
        return byteArray;
    }


}
