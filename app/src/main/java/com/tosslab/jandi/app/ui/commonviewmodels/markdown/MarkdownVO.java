package com.tosslab.jandi.app.ui.commonviewmodels.markdown;

/**
 * Created by tee on 15. 12. 7..
 */
public class MarkdownVO {

    private String markdownString = null;
    private int startIndex = 0;
    private int endIndex = 0;
    private TYPE type;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getMarkdownString() {
        return markdownString;
    }

    public void setMarkdownString(String markdownString) {
        this.markdownString = markdownString;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public static enum TYPE {
        ITALIC, BOLD, ITALICBOLD, STRIKE
    }

}
