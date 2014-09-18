package com.tosslab.jandi.app.lists.invitations;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
@EViewGroup(R.layout.item_invitation)
public class InvitationView extends LinearLayout {
    @ViewById(R.id.txt_invitation_email_address)
    TextView textViewInvitationEmail;
    @ViewById(R.id.btn_invitation_remove)
    Button buttonInvitationRemove;

    public InvitationView(Context context) {
        super(context);
    }

    public void bind(String email) {
        textViewInvitationEmail.setText(email);
        buttonInvitationRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
