package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.topics.domain.Topic;

/**
 * Created by Steve SeongUg Jung on 15. 7. 8..
 */
public class UnjoinTopicDialog extends DialogFragment {

    private static final String ARGS_NAME = "name";
    private static final String ARGS_DESCRIPTION = "description";
    private static final String ARGS_CREATOR_ID = "creatorId";
    private static final String ARGS_MEMBER_COUNT = "memberCount";

    private DialogInterface.OnClickListener onJoinClickListener;

    public static UnjoinTopicDialog instantiate(Topic topic) {

        UnjoinTopicDialog fragment = new UnjoinTopicDialog();
        Bundle args = new Bundle();

        args.putString(ARGS_NAME, topic.getName());
        args.putString(ARGS_DESCRIPTION, topic.getDescription());
        args.putInt(ARGS_CREATOR_ID, topic.getCreatorId());
        args.putInt(ARGS_MEMBER_COUNT, topic.getMemberCount());

        fragment.setArguments(args);

        return fragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        String name = arguments.getString(ARGS_NAME);
        String description = arguments.getString(ARGS_DESCRIPTION);
        int creatorId = arguments.getInt(ARGS_CREATOR_ID);
        int memberCount = arguments.getInt(ARGS_MEMBER_COUNT);

        View rootView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_unjoined_topic_description, null);

        TextView tvTitle = (TextView) rootView.findViewById(R.id.tv_unjoin_topic_desc_title);
        TextView tvHost = (TextView) rootView.findViewById(R.id.tv_unjoin_topic_desc_host);
        TextView tvMemberCount = (TextView) rootView.findViewById(R.id.tv_unjoin_topic_desc_member_count);
        TextView tvDescription = (TextView) rootView.findViewById(R.id.tv_unjoin_topic_desc_content);
        tvDescription.setMovementMethod(new ScrollingMovementMethod());

        tvTitle.setText(name);
        tvMemberCount.setText(String.valueOf(memberCount));
        tvHost.setText(EntityManager.getInstance().getEntityNameById(creatorId));

        if (TextUtils.isEmpty(description)) {
            tvDescription.setText(R.string.jandi_it_has_no_topic_description);
        } else {
            tvDescription.setText(description);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView)
                .setPositiveButton("Join", onJoinClickListener);

        return builder.create();
    }

    public void setOnJoinClickListener(DialogInterface.OnClickListener onJoinClickListener) {
        this.onJoinClickListener = onJoinClickListener;
    }
}
