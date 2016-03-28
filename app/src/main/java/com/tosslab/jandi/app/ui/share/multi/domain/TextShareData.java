package com.tosslab.jandi.app.ui.share.multi.domain;

public class TextShareData implements ShareData {
    private String text;

    public TextShareData(String text) {
        this.text = text;
    }

    @Override
    public String getData() {
        return text;
    }
}
