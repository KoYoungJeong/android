package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class EntitySpannable extends UnderlineSpan implements ClickableSpannable {

    private final boolean isStarred;
    private Context context;
    private long entityId;
    private int entityType;
    private long teamId;
    private int color;

    public EntitySpannable(Context context, long teamId, long entityId, int entityType, boolean isStarred) {
        this.context = context;
        this.entityId = entityId;
        this.entityType = entityType;
        this.teamId = teamId;
        this.isStarred = isStarred;
        color = context.getResources().getColor(R.color.jandi_file_detail_share_text);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
        ds.linkColor = color;
        ds.setUnderlineText(true);
        ds.setShadowLayer(10, 1, 1, Color.WHITE);
        ds.setTextSize(context.getResources().getDimension(R.dimen.jandi_file_detail_file_share_text));
    }

    @Override
    public void onClick() {
        EventBus.getDefault().post(new MoveSharedEntityEvent(entityId));
    }
}
