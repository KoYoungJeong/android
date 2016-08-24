package com.tosslab.jandi.app.spannable.analysis.mention;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.spannable.analysis.RuleAnalysis;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.spannable.ClickableMentionMessageSpannable;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import java.util.Collection;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class MentionAnalysis implements RuleAnalysis {

    public static final String TAG = MentionAnalysis.class.getSimpleName();

    private MentionAnalysisInfo mentionAnalysisInfo;

    public void setMentionAnalysisInfo(MentionAnalysisInfo mentionAnalysisInfo) {
        this.mentionAnalysisInfo = mentionAnalysisInfo;
    }

    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        if (mentionAnalysisInfo == null) {
            return;
        }

        Collection<MentionObject> mentions = mentionAnalysisInfo.getMentions();
        if (mentions == null || mentions.isEmpty()) {
            return;
        }

        for (MentionObject mention : mentions) {
            String name = getName(mention, spannableStringBuilder);
            if (TextUtils.isEmpty(name)) {
                continue;
            }

            boolean mentionForMe = false;

            // all일 경우 나에게 온걸로 가정한다.
            if (TeamInfoLoader.getInstance().isTopic(mention.getId())) {
                mentionForMe = true;
            } else {
                mentionForMe =
                        mention.getId() == mentionAnalysisInfo.getMyId();
            }

            int textColor = mentionForMe
                    ? mentionAnalysisInfo.getForMeTextColor()
                    : mentionAnalysisInfo.getTextColor();

            int backgroundColor = mentionForMe
                    ? mentionAnalysisInfo.getForMeBackgroundColor()
                    : mentionAnalysisInfo.getBackgroundColor();

            MentionMessageSpannable mentionMessageSpannable =
                    new ClickableMentionMessageSpannable(name, mention.getId(),
                            mentionAnalysisInfo.getTextSize(), textColor, backgroundColor);

            float pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260,
                    context.getResources().getDisplayMetrics());
            mentionMessageSpannable.setViewMaxWidthSize((int) pixel);

            int start = mention.getOffset();
            int end = mention.getLength() + mention.getOffset();
            spannableStringBuilder.setSpan(
                    mentionMessageSpannable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private
    @Nullable
    String getName(MentionObject mention,
                   SpannableStringBuilder spannableStringBuilder) {
        try {
            int start = mention.getOffset() + 1;
            int end = mention.getLength() + mention.getOffset();
            return spannableStringBuilder.subSequence(start, end).toString();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

}
