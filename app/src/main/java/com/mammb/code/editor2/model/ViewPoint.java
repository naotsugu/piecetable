package com.mammb.code.editor2.model;

/**
 * ViewPoint.
 * @author Naotsugu Kobayashi
 */
public class ViewPoint implements EventListener<Edit> {

    /** The text content. */
    private Content content;

    /** The code point index of start on view. */
    private int contentIndex;

    /** The target view. */
    private TextView textView;


    /**
     * Constructor.
     * @param content the text content
     */
    public ViewPoint(Content content) {
        this.content = content;
        this.contentIndex = 0;
        this.textView = new TextView(this);
    }

    @Override
    public void handle(Edit edit) {
        content.handle(
            edit.posWith(contentIndex + textView.codePointCount(0, edit.pos())));
    }

    /**
     * Get the text view.
     * @return the text view
     */
    public TextView textView() {
        return textView;
    }

    /**
     * Get the code point length of view.
     */
    private int length() {
        return textView.codePointCount();
    }

}
