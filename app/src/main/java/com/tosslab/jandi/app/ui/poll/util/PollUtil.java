package com.tosslab.jandi.app.ui.poll.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.poll.PollDataChangedEvent;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.dynamicl10n.PollFinished;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collection;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class PollUtil {

    public static SpannableStringBuilder buildFormatMessage(Context context, PollFinished pollFinished,
                                    ResMessages.CommentMessage commentMessage, long myId,
                                    float mentionTextSize) {
        SpannableStringBuilder messageBuilder = new SpannableStringBuilder();
        Collection<MentionObject> mentions = commentMessage.mentions;
        if (mentions != null && !mentions.isEmpty()) {
            Observable.from(mentions)
                    .takeFirst(mentionObject -> true)
                    .subscribe(mentionObject -> {
                        int start = mentionObject.getOffset();
                        int end = start + mentionObject.getLength();
                        String mention =
                                commentMessage.content.body.substring(start, end);
                        messageBuilder.append(mention + " ");
                    });
        }

        Resources resources = context.getResources();
        int votedCount = pollFinished.getVotedCount();
        if (votedCount <= 0) {
            String message = resources.getString(R.string.jandi_poll_finished_without_participants);
            messageBuilder.append(message);
        } else {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (PollFinished.ElectedItem item : pollFinished.getElectedItems()) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(item.getName());
                i++;
            }
            String message = resources.getString(R.string.jandi_poll_finished_with_most, sb.toString());
            messageBuilder.append(message);
        }

        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                        .textSize(mentionTextSize)
                        .clickable(true)
                        .build();

        SpannableLookUp.text(messageBuilder)
                .mention(mentionAnalysisInfo, false)
                .lookUp(context);

        return messageBuilder;
    }


}
