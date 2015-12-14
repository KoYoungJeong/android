package com.tosslab.jandi.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

/**
 * Created by tee on 15. 7. 16..
 */
public class TutorialCoachMarkUtil {

    private static final int COACH_MARK_TOPIC_LIST = 0x0;
    private static final int COACH_MARK_DIRECT_MESSAGE_LIST = 0x1;
    private static final int COACH_MARK_FILE_LIST = 0x2;
    private static final int COACH_MARK_MORE = 0x3;
    private static final int COACH_MARK_TOPIC = 0x4;

    private static Dialog showCoachMarkDialog(Context context, int coachMarkType) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (coachMarkType == COACH_MARK_FILE_LIST) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x50000000));
        } else {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x48000000));
        }

        switch (coachMarkType) {
            case COACH_MARK_TOPIC_LIST:
                dialog.setContentView(R.layout.dialog_coach_mark_topiclist);
                break;
            case COACH_MARK_FILE_LIST:
                dialog.setContentView(R.layout.dialog_coach_mark_filelist);
                break;
            case COACH_MARK_DIRECT_MESSAGE_LIST:
                dialog.setContentView(R.layout.dialog_coach_mark_messagelist);
                break;
            case COACH_MARK_MORE:
                dialog.setContentView(R.layout.dialog_coach_mark_more);
                break;
            case COACH_MARK_TOPIC:
                dialog.setContentView(R.layout.dialog_coach_mark_topic);
                break;
        }

        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(view -> dialog.dismiss());

        EntityManager entityManager = EntityManager.getInstance();

        if (coachMarkType == COACH_MARK_MORE) {
            ImageView profileImageView = (ImageView) masterView.findViewById(R.id.iv_profile_guide_image_icon);
            if (profileImageView != null) {
                if (entityManager != null) {
                    FormattedEntity me = entityManager.getMe();
                    Ion.with(profileImageView)
                            .placeholder(R.drawable.profile_img)
                            .error(R.drawable.profile_img)
                            .transform(new IonCircleTransform())
                            .load(me.getUserSmallProfileUrl());
                }
            }
        }

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
        return dialog;
    }

    public static void showCoachMarkTopicListIfNotShown(Context context) {
        if (!JandiPreference.isAleadyShowCoachMarkTopicList(context.getApplicationContext())) {
            showCoachMarkDialog(context, COACH_MARK_TOPIC_LIST);
        }
    }

    public static void showCoachMarkDirectMessageListIfNotShown(Context context) {
        if (!JandiPreference.isAleadyShowCoachMarkDirectMessageList(context.getApplicationContext())) {
            showCoachMarkDialog(context, COACH_MARK_DIRECT_MESSAGE_LIST);
        }
    }

    public static void showCoachMarkFileListIfNotShown(Context context) {
        if (!JandiPreference.isAleadyShowCoachMarkFileList(context.getApplicationContext())) {
            showCoachMarkDialog(context, COACH_MARK_FILE_LIST);
        }
    }

    public static void showCoachMarkMoreIfNotShown(Context context) {
        if (!JandiPreference.isAleadyShowCoachMarkMore(context.getApplicationContext())) {
            showCoachMarkDialog(context, COACH_MARK_MORE);
        }
    }

    public static void showCoachMarkTopicIfNotShown(boolean user, Context context) {
        if (!JandiPreference.isAleadyShowCoachMarkTopic(context.getApplicationContext())) {
            Dialog dialog = showCoachMarkDialog(context, COACH_MARK_TOPIC);
            if (user) {
                dialog.findViewById(R.id.tv_topic_sticker_guide).setVisibility(View.GONE);
                dialog.findViewById(R.id.iv_topic_sticker_guide_icon).setVisibility(View.GONE);
            }
        }
    }

}
