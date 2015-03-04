package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
public class AccountPendingTeamRowView extends RelativeLayout {

    private TextView nameTextView;
    private OnJoinClickListener joinClickListener;

    public AccountPendingTeamRowView(Context context) {
        this(context, null);
    }

    public AccountPendingTeamRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
        initView();
    }

    private void inflateView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_account_home_pending_team, this, true);
    }

    private void initView() {
        nameTextView = ((TextView) findViewById(R.id.txt_team_list_name));

        findViewById(R.id.btn_team_list_ignore).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (joinClickListener != null) {
                    joinClickListener.onJoinClick(AccountPendingTeamRowView.this, false);
                }
            }
        });

        findViewById(R.id.btn_team_list_accept).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (joinClickListener != null) {
                    joinClickListener.onJoinClick(AccountPendingTeamRowView.this, true);
                }
            }
        });
    }

    public void setTeamName(String teamName) {
        nameTextView.setText(teamName);
    }

    public void setOnJoinClickListener(OnJoinClickListener joinClickListener) {
        this.joinClickListener = joinClickListener;
    }

    public interface OnJoinClickListener {
        void onJoinClick(View view, boolean join);
    }
}
