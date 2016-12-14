package com.tosslab.jandi.app.ui.invites.emails.adapter.binder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2016. 12. 9..
 */

public class ItemViewBinder {

    @Bind(R.id.vg_content)
    ViewGroup vgContent;
    @Bind(R.id.tv_email)
    TextView tvEmail;
    @Bind(R.id.v_status_dot)
    ImageView vStatusDot;
    @Bind(R.id.tv_status_message)
    TextView tvStatusMessage;
    @Bind(R.id.vg_invite_email_cancel)
    ViewGroup vgInviteEmailCancel;

    private Context context;

    private InviteCancelListener inviteCancelListener;

    public ItemViewBinder(Context context) {
        this.context = context;
    }

    private View getItemView() {
        return LayoutInflater.from(context).inflate(R.layout.item_invite_email, null);
    }

    public View bindView(InviteEmailVO item) {
        View view = getItemView();
        ButterKnife.bind(this, view);

        if (item.getStatus() == InviteEmailVO.Status.AVAILABLE) {
            vStatusDot.setImageDrawable(JandiApplication.getContext()
                    .getResources().getDrawable(R.drawable.email_validation_blue));
            tvStatusMessage.setTextColor(0xff00ace9);
            tvStatusMessage.setText(JandiApplication.getContext()
                    .getText(R.string.invite_email_valid));
        } else if (item.getStatus() == InviteEmailVO.Status.JOINED) {
            vStatusDot.setImageDrawable(JandiApplication.getContext()
                    .getResources().getDrawable(R.drawable.email_validation_red));
            tvStatusMessage.setTextColor(0xfff15544);
            tvStatusMessage.setText(JandiApplication.getContext()
                    .getText(R.string.invite_email_alreadyinteam));
        } else if (item.getStatus() == InviteEmailVO.Status.DUMMY) {
            vStatusDot.setImageDrawable(JandiApplication.getContext()
                    .getResources().getDrawable(R.drawable.email_validation_grey));
            tvStatusMessage.setTextColor(0xff999999);
            tvStatusMessage.setText(JandiApplication.getContext()
                    .getText(R.string.invite_email_alreadyinvited));
        } else if (item.getStatus() == InviteEmailVO.Status.BLOCKED) {
            vStatusDot.setImageDrawable(JandiApplication.getContext()
                    .getResources().getDrawable(R.drawable.email_validation_red));
            tvStatusMessage.setTextColor(0xfff15544);
            tvStatusMessage.setText(JandiApplication.getContext()
                    .getText(R.string.invite_email_blocked));
        }

        tvEmail.setText(item.getEmail());

        vgInviteEmailCancel.setOnClickListener(v -> {
            inviteCancelListener.cancel(view);
        });
        return view;
    }

    public void setInviteCancelListener(InviteCancelListener inviteCancelListener) {
        this.inviteCancelListener = inviteCancelListener;
    }

    public interface InviteCancelListener {
        void cancel(View view);
    }

}
