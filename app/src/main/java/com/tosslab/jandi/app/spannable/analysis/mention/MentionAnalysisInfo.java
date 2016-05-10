package com.tosslab.jandi.app.spannable.analysis.mention;

import android.graphics.Color;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Collection;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class MentionAnalysisInfo {

    private final long myId;
    private final Collection<MentionObject> mentions;
    private final int textSizeFromResource;
    private final float textSize;
    private final int textColor;
    private final int backgroundColor;
    private final int forMeTextColor;
    private final int forMeBackgroundColor;
    private final boolean clickable;

    private MentionAnalysisInfo(Builder builder) {
        myId = builder.myId;
        mentions = builder.mentions;
        textSizeFromResource = builder.textSizeFromResource != 0
                ? JandiApplication.getContext()
                .getResources().getDimensionPixelSize(builder.textSizeFromResource)
                : 0;
        textSize = builder.textSize;
        textColor = builder.textColor != 0 ? builder.textColor : Color.parseColor("#FF00A6E9");
        backgroundColor = builder.backgroundColor != 0
                ? builder.backgroundColor : Color.TRANSPARENT;
        forMeTextColor = builder.forMeTextColor != 0
                ? builder.forMeTextColor : Color.parseColor("#FF00A6E9");
        forMeBackgroundColor = builder.forMeBackgroundColor != 0
                ? builder.forMeBackgroundColor : Color.parseColor("#FFDAF2FF");
        clickable = builder.clickable;
    }

    public static Builder newBuilder(long myId, Collection<MentionObject> mentions) {
        return new Builder(myId, mentions);
    }

    public long getMyId() {
        return myId;
    }

    public Collection<MentionObject> getMentions() {
        return mentions;
    }

    public float getTextSize() {
        if (textSizeFromResource != 0) {
            return textSizeFromResource;
        }
        return textSize;
    }

    public boolean isClickable() {
        return clickable;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getForMeTextColor() {
        return forMeTextColor;
    }

    public int getForMeBackgroundColor() {
        return forMeBackgroundColor;
    }

    public static class Builder {
        private long myId;
        private Collection<MentionObject> mentions;
        private int textSizeFromResource;
        private float textSize;
        private int textColor;
        private int backgroundColor;
        private int forMeTextColor;
        private int forMeBackgroundColor;
        private boolean clickable;

        public Builder(long myId, Collection<MentionObject> mentions) {
            this.myId = myId;
            this.mentions = mentions;
        }

        public Builder textSizeFromResource(int dimen) {
            this.textSizeFromResource = dimen;
            return this;
        }

        public Builder textSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder forMeTextColor(int forMeTextColor) {
            this.forMeTextColor = forMeTextColor;
            return this;
        }

        public Builder forMeBackgroundColor(int forMeBackgroundColor) {
            this.forMeBackgroundColor = forMeBackgroundColor;
            return this;
        }

        public Builder clickable(boolean clickable) {
            this.clickable = clickable;
            return this;
        }

        public MentionAnalysisInfo build() {
            return new MentionAnalysisInfo(this);
        }
    }

}
