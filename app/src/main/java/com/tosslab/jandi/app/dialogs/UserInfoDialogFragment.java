package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResMemberProfile;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 3..
 */
public class UserInfoDialogFragment extends DialogFragment {
    private final static String ARG_USER_ID = "userId";
    private final static String ARG_USER_NAME = "userName";
    private final static String ARG_USER_STATUS_MSG = "userStatusMessage";
    private final static String ARG_USER_DIVISION = "userDivision";
    private final static String ARG_USER_POSITION = "userPosition";
    private final static String ARG_USER_PHONE_NUMBER = "userPhoneNumber";
    private final static String ARG_USER_EMAIL = "userEmail";
    private final static String ARG_USER_PROFILE_URL = "profileUrl";
    private final static String ARG_USER_IS_ME = "isMe";

    public static UserInfoDialogFragment newInstance(FormattedEntity user, boolean isMe) {
        UserInfoDialogFragment frag = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, user.getId());
        args.putString(ARG_USER_NAME, user.getName());
        args.putString(ARG_USER_STATUS_MSG, user.getUserStatusMessage());
        args.putString(ARG_USER_DIVISION, user.getUserDivision());
        args.putString(ARG_USER_POSITION, user.getUserPosition());
        args.putString(ARG_USER_PHONE_NUMBER, user.getUserPhoneNumber());
        args.putString(ARG_USER_EMAIL, user.getUserEmail());
        args.putString(ARG_USER_PROFILE_URL, user.getUserLargeProfileUrl());
        args.putBoolean(ARG_USER_IS_ME, isMe);

        frag.setArguments(args);
        return frag;
    }

    public static UserInfoDialogFragment newInstance(ResMemberProfile user, boolean isMe) {
        UserInfoDialogFragment frag = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, user.id);
        args.putString(ARG_USER_NAME, user.name);
        args.putString(ARG_USER_STATUS_MSG, user.u_statusMessage);
        args.putString(ARG_USER_DIVISION, !TextUtils.isEmpty(user.u_extraData.department) ? user.u_extraData.department : "");
        args.putString(ARG_USER_POSITION, !TextUtils.isEmpty(user.u_extraData.position) ? user.u_extraData.position : "");
        args.putString(ARG_USER_PHONE_NUMBER, !TextUtils.isEmpty(user.u_extraData.phoneNumber) ? user.u_extraData.phoneNumber : "");
        args.putString(ARG_USER_EMAIL, user.u_email);
        args.putString(ARG_USER_PROFILE_URL, !TextUtils.isEmpty(user.u_photoThumbnailUrl.largeThumbnailUrl) ? user.u_photoThumbnailUrl.largeThumbnailUrl : user.u_photoUrl);
        args.putBoolean(ARG_USER_IS_ME, isMe);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // 회면 밖 터치시 다이얼로그 종료
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final int userId = bundle.getInt(ARG_USER_ID, -1);
        final String userName = bundle.getString(ARG_USER_NAME);
        final String userNickname = bundle.getString(ARG_USER_STATUS_MSG);
        final String userDivision = bundle.getString(ARG_USER_DIVISION);
        final String userPosition = bundle.getString(ARG_USER_POSITION);
        final String userPhoneNumber = bundle.getString(ARG_USER_PHONE_NUMBER);
        final String userEmail = bundle.getString(ARG_USER_EMAIL);
        final String userProfileUrl = bundle.getString(ARG_USER_PROFILE_URL);
        final boolean isMe = bundle.getBoolean(ARG_USER_IS_ME, false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_user_profile, null);

        final ImageView imgUserPhoto = (ImageView) mainView.findViewById(R.id.img_user_info_photo);
        final TextView txtUserName = (TextView) mainView.findViewById(R.id.txt_user_info_name);
        final TextView txtUserNickname = (TextView) mainView.findViewById(R.id.txt_user_info_nickname);
        final TextView txtUserDivision = (TextView) mainView.findViewById(R.id.txt_user_info_division);
        final TextView txtUserPosition = (TextView) mainView.findViewById(R.id.txt_user_info_position);
        final TextView txtUserEmail = (TextView) mainView.findViewById(R.id.txt_user_info_email);
        final TextView txtUserPhone = (TextView) mainView.findViewById(R.id.txt_user_info_phone);
        final LinearLayout lyUserEmail = (LinearLayout) mainView.findViewById(R.id.ly_user_info_mail);
        final LinearLayout lyUserPhone = (LinearLayout) mainView.findViewById(R.id.ly_user_info_phone);
        final LinearLayout lyUserDirectMessage = (LinearLayout) mainView.findViewById(R.id.ly_user_info_direct_message);
        final View borderUserDirectMessage = mainView.findViewById(R.id.border_user_info_direct_message);

        if (isMe) {     // 본인의 정보면 1:1 대화 버튼을 보여주지 않는다.
            lyUserDirectMessage.setVisibility(View.GONE);
            borderUserDirectMessage.setVisibility(View.GONE);
        } else {
            lyUserDirectMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new RequestMoveDirectMessageEvent(userId));
                    dismiss();
                }
            });
        }

        if (userPhoneNumber == null || userPhoneNumber.length() <= 0) {
            lyUserPhone.setVisibility(View.GONE);
        } else {
            // Set OnClickListener for Phone Call
            txtUserPhone.setText(userPhoneNumber);
            lyUserPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = "tel:" + userPhoneNumber.replaceAll("[^0-9|\\+]", "");
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                    startActivity(intent);
                }
            });
        }

        if (userEmail == null || userEmail.length() <= 0) {
            lyUserEmail.setVisibility(View.GONE);
        } else {
            txtUserEmail.setText(userEmail);
            lyUserEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = "mailto:" + userEmail;
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
                    startActivity(intent);
                }
            });
        }

        txtUserName.setText(userName);
        txtUserNickname.setText(userNickname);
        txtUserDivision.setText(userDivision);
        txtUserPosition.setText(userPosition);
        Glide.with(getActivity())
                .load(userProfileUrl)
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(getActivity()))
                .into(imgUserPhoto);

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }
}
