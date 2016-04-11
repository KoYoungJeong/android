package com.tosslab.jandi.app.ui.invites.email.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.invites.email.model.InvitedEmailDataModel;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;
import com.tosslab.jandi.app.ui.invites.email.view.InvitedEmailView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class InvitedEmailListAdapter extends RecyclerView.Adapter<BaseViewHolder<EmailVO>>
        implements InvitedEmailDataModel, InvitedEmailView {

    private List<EmailVO> invitedEmailList = new ArrayList<>();

    @Override
    public EmailVO getInvitedEmail(int position) {
        return invitedEmailList.get(position);
    }

    @Override
    public BaseViewHolder<EmailVO> onCreateViewHolder(ViewGroup parent, int viewType) {
        return InvitedEmailViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<EmailVO> holder, int position) {
        holder.onBindView(getInvitedEmail(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return invitedEmailList.size();
    }

    @Override
    public void add(int position, EmailVO emailText) {
        invitedEmailList.add(position, emailText);
    }

    @Override
    public void remove(int index) {
        invitedEmailList.remove(index);
    }

    @Override
    public void clear() {
        invitedEmailList.clear();
    }

    @Override
    public List<EmailVO> getInvitedEmailList() {
        return invitedEmailList;
    }

    @Override
    public int updateEmailToInviteSuccessAndGetPosition(final String email) {
        for (int i = invitedEmailList.size() - 1; i >= 0; i--) {
            EmailVO emailVO = invitedEmailList.get(i);
            String itemsEmail = emailVO.getEmail();
            if (TextUtils.equals(itemsEmail, email)) {
                emailVO.setSuccess(1);
                return i;
            }
        }
        return -1;
    }

    @Override
    public int updateEmailToInviteFailAndGetPosition(String email) {
        for (int i = invitedEmailList.size() - 1; i >= 0; i--) {
            EmailVO emailVO = invitedEmailList.get(i);
            String itemsEmail = emailVO.getEmail();
            if (TextUtils.equals(itemsEmail, email)) {
                remove(i);
                return i;
            }
        }
        return 0;
    }

    @Override
    public EmailVO findEmailVoByEmail(String email) {
        for (int i = invitedEmailList.size() - 1; i >= 0; i--) {
            EmailVO emailVO = invitedEmailList.get(i);
            String itemsEmail = emailVO.getEmail();
            if (TextUtils.equals(itemsEmail, email)) {
                return emailVO;
            }
        }
        return null;
    }

    @Override
    public int add(EmailVO emailTO) {
        invitedEmailList.add(emailTO);
        return invitedEmailList.size() - 1;
    }

    public static class InvitedEmailViewHolder extends BaseViewHolder<EmailVO> {

        @Bind(R.id.tv_invite_email)
        TextView tvEmail;
        @Bind(R.id.progress_invite_processing)
        ProgressBar pbSending;
        @Bind(R.id.iv_invite_success)
        ImageView ivSuccess;

        private InvitedEmailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private static InvitedEmailViewHolder newInstance(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invite_list, parent, false);
            return new InvitedEmailViewHolder(itemView);
        }

        @Override
        public void onBindView(EmailVO emailVO) {
            tvEmail.setText(emailVO.getEmail());

            boolean success = emailVO.getSuccess() == 1;

            ivSuccess.setImageResource(
                    success ? R.drawable.icon_accept : R.drawable.alert_icon_warning);
            ivSuccess.setVisibility(success ? View.VISIBLE : View.GONE);
            pbSending.setVisibility(success ? View.GONE : View.VISIBLE);
        }
    }
}
