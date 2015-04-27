package com.tosslab.jandi.app.ui.filedetail;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class EntitySpannable extends ClickableSpan {

    private final boolean isStarred;
    private Context context;
    private int entityId;
    private int entityType;
    private int teamId;

    public EntitySpannable(Context context, int teamId, int entityId, int entityType, boolean isStarred) {
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

        MessageListV2Activity_.intent(context)
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityId : -1)
                .isFromPush(false)
                .isFavorite(isStarred)
                .start();
    }
}
