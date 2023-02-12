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
package com.mammb.code.editor3.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;
import javafx.util.Duration;

import java.util.function.Function;

/**
 * UiCaret.
 * @author Naotsugu Kobayashi
 */
public class UiCaret extends Path {

    /** The timeline. */
    private final Timeline timeline = new Timeline();

    private final Function<Point2D, HitInfo> hitTestFun;

    private final Function<Integer, PathElement[]> caretShapeFun;

    private double physicalX;
    private double physicalY;
    private double height;

    private double logicalX;


    /**
     * Constructor.
     */
    public UiCaret(TextFlow textFlow) {

        setStrokeWidth(2);
        setStroke(Color.ORANGE);
        setManaged(false);

        this.hitTestFun = textFlow.hitTestFun();
        this.caretShapeFun = textFlow.caretShapeFun();

        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();

        moveTo(0, 0);
    }


    void disable() {
        timeline.stop();
        setVisible(false);
        getElements().remove(0, getElements().size());
    }


    public int down() {
        return moveTo(logicalX, physicalY + height);
    }


    public int up() {
        return moveTo(logicalX, physicalY - 1);
    }


    private int moveTo(double x, double y) {
        HitInfo hit = hitTestFun.apply(new Point2D(x, y));
        int insertionIndex = hit.getInsertionIndex();
        setShape(caretShapeFun.apply(insertionIndex));
        return insertionIndex;
    }


    private void setShape(PathElement... elements) {

        if (elements == null || elements.length == 0) {
            disable();
            return;
        }
        if (elements[0] instanceof MoveTo moveTo) {
            physicalX = moveTo.getX();
            physicalY = moveTo.getY();
        }
        if (elements[1] instanceof LineTo lineTo) {
            height = lineTo.getY() - physicalY;
        }

        timeline.stop();
        getElements().setAll(elements);
        setVisible(true);
        timeline.play();
    }

}
