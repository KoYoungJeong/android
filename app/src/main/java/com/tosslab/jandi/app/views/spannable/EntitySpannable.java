package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class EntitySpannable extends ClickableSpan {

    private final boolean isStarred;
    private Context context;
    private long entityId;
    private int entityType;
    private long teamId;

    public EntitySpannable(Context context, long teamId, long entityId, int entityType, boolean isStarred) {
        this.context = context;
        this.entityId = entityId;
        this.entityType = entityType;
        this.teamId = teamId;
        this.isStarred = isStarred;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.linkColor = context.getResources().getColor(R.color.jandi_file_detail_share_text);
        ds.setUnderlineText(true);
        ds.setShadowLayer(10, 1, 1, Color.WHITE);
        ds.setTextSize(context.getResources().getDimension(R.dimen.jandi_file_detail_file_share_text));
    }

    @Override
    public void onClick(View widget) {

        EventBus.getDefault().post(new MoveSharedEntityEvent(entityId));

    }
}
