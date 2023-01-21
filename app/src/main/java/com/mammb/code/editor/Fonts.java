package com.mammb.code.editor;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class Fonts {

    private static final String fontName = System.getProperty("os.name").startsWith("Windows") ? "MS Gothic" : "Consolas";
    public static final int fontSize = 16;
    public static final Font main = Font.font(fontName, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);

}
