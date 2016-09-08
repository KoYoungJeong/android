package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view;

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
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

public class TopicInfoDialog extends DialogFragment {

    private static final String ARGS_NAME = "name";
    private static final String ARGS_DESCRIPTION = "description";
    private static final String ARGS_CREATOR_ID = "creatorId";
    private static final String ARGS_MEMBER_COUNT = "memberCount";
    private static final String ARGS_ENTITY_ID = "entityId";
    private OnJoinClickListener onJoinClickListener;
    private OnDismissListener onDismissListener;

    public static TopicInfoDialog instantiate(TopicRoom topic) {
        TopicInfoDialog fragment = new TopicInfoDialog();
        Bundle args = new Bundle();

        args.putString(ARGS_NAME, topic.getName());
        args.putString(ARGS_DESCRIPTION, topic.getDescription());
        args.putLong(ARGS_CREATOR_ID, topic.getCreatorId());
        args.putLong(ARGS_ENTITY_ID, topic.getId());
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
        long creatorId = arguments.getLong(ARGS_CREATOR_ID);
        final long entityId = arguments.getLong(ARGS_ENTITY_ID);
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
        tvHost.setText(TeamInfoLoader.getInstance().getMemberName(creatorId));

        if (TextUtils.isEmpty(description)) {
            tvDescription.setText(R.string.jandi_it_has_no_topic_description);
        } else {
            tvDescription.setText(description);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setView(rootView)
                .setPositiveButton(R.string.jandi_join_topic, ((dialog, which) -> {
                    if (onJoinClickListener != null) {
                        onJoinClickListener.onJoinClick(entityId);
                    }
                }));
        builder.setOnDismissListener(dialog -> {

        });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDismissListener != null) {
            long entityId = getArguments().getLong(ARGS_ENTITY_ID, -1);
            onDismissListener.onDismiss(entityId);
        }
        super.onDismiss(dialog);
    }

    public void setOnJoinClickListener(OnJoinClickListener onJoinClickListener) {
        this.onJoinClickListener = onJoinClickListener;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public interface OnJoinClickListener {
        void onJoinClick(long topicEntityId);
    }

    public interface OnDismissListener {
        void onDismiss(long topicEntityId);
    }

}
