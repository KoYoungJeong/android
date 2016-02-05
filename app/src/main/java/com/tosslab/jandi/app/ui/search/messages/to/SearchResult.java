package com.tosslab.jandi.app.ui.search.messages.to;

import android.text.SpannableStringBuilder;

import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 3. 27..
 */
public class SearchResult {
    private String topicName;
    private Date date;
    private SpannableStringBuilder previewText;
    private SpannableStringBuilder currentText;
    private SpannableStringBuilder nextText;

    private long entityId;
    private long linkId;

    public Date getDate() {
        return date;
    }

    public SearchResult date(Date date) {
        this.date = date;
        return this;
    }

    public SpannableStringBuilder getPreviewText() {
        return previewText;
    }

    public SearchResult previewText(SpannableStringBuilder previewText) {
        this.previewText = previewText;
        return this;
    }

    public SpannableStringBuilder getCurrentText() {
        return currentText;
    }

    public SearchResult currentText(SpannableStringBuilder currentText) {
        this.currentText = currentText;
        return this;
    }

    public SpannableStringBuilder getNextText() {
        return nextText;
    }

    public SearchResult nextText(SpannableStringBuilder nextText) {
        this.nextText = nextText;
        return this;
    }

    public String getTopicName() {
        return topicName;
    }

    public SearchResult topicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
