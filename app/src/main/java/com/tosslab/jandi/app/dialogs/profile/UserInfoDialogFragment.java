package com.tosslab.jandi.app.dialogs.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by justinygchoi on 2014. 9. 3..
 */
@EFragment
public class UserInfoDialogFragment extends DialogFragment {


    private static final Logger logger = Logger.getLogger(UserInfoDialogFragment.class);

    @FragmentArg
    int entityId;

    @Bean
    JandiEntityClient jandiEntityClient;
    private ImageView imgStarred;

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final int userId = entityId;

        FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);

        if (TextUtils.equals(entity.getUser().status, "enabled")) {
            return createEnabledUserDialog(userId, entity);
        } else {
            return createDisabledUserDialog(userId, entity);
        }

    }

    private Dialog createEnabledUserDialog(int userId, FormattedEntity entity) {
        final String userName = entity.getName();
        final String userNickname = entity.getUserStatusMessage();
        final String userDivision = entity.getUserDivision();
        final String userPosition = entity.getUserPosition();
        final String userPhoneNumber = entity.getUserPhoneNumber();
        final String userEmail = entity.getUserEmail();
        final String userProfileUrl = entity.getUserLargeProfileUrl();
        final boolean isMe = EntityManager.getInstance(getActivity()).isMe(userId);
        final boolean isStarred = entity.isStarred;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_user_profile, null);

        final ImageView imgUserPhoto = (ImageView) mainView.findViewById(R.id.img_user_info_photo);
        imgStarred = (ImageView) mainView.findViewById(R.id.img_user_info_starred);
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

        if (!isMe) {
            setProfileStarred(isStarred);
        } else {
            imgStarred.setVisibility(View.GONE);
        }

        imgStarred.setOnClickListener(v -> onStarClick(userId));

        if (isMe) {     // 본인의 정보면 1:1 대화 버튼을 보여주지 않는다.
            lyUserDirectMessage.setVisibility(View.GONE);
            borderUserDirectMessage.setVisibility(View.GONE);
        } else {
            lyUserDirectMessage.setOnClickListener(view -> {
                EventBus.getDefault().post(new RequestMoveDirectMessageEvent(userId));
                dismiss();
            });
        }

        if (userPhoneNumber == null || userPhoneNumber.length() <= 0) {
            lyUserPhone.setVisibility(View.GONE);
        } else {
            // Set OnClickListener for Phone Call
            txtUserPhone.setText(userPhoneNumber);
            lyUserPhone.setOnClickListener(view -> {
                String uri = "tel:" + userPhoneNumber.replaceAll("[^0-9|\\+]", "");
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                startActivity(intent);
            });
        }

        if (userEmail == null || userEmail.length() <= 0) {
            lyUserEmail.setVisibility(View.GONE);
        } else {
            txtUserEmail.setText(userEmail);
            lyUserEmail.setOnClickListener(view -> {
                String uri = "mailto:" + userEmail;
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
                startActivity(intent);
            });
        }

        txtUserName.setText(userName);
        txtUserNickname.setText(userNickname);
        txtUserDivision.setText(userDivision);
        txtUserPosition.setText(userPosition);
        Ion.with(imgUserPhoto)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(userProfileUrl);

        if (!TextUtils.isEmpty(userProfileUrl)) {
            imgUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Dialog alertDialog = new Dialog(getActivity());
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_profile_view, null);
                    PhotoView profileView = (PhotoView) view.findViewById(R.id.photo_dialog_profile_view);
                    ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_dialog_profile_view);

                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(view);

                    alertDialog.setCanceledOnTouchOutside(false);
                    Window alertWindow = alertDialog.getWindow();

                    DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
                    WindowManager.LayoutParams attributes = alertWindow.getAttributes();
                    attributes.width = displayMetrics.widthPixels;
                    attributes.height = displayMetrics.heightPixels;
                    alertWindow.setAttributes(attributes);
                    alertWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Ion.with(profileView)
                                    .deepZoom()
                                    .load(userProfileUrl)
                                    .setCallback((e, result) -> progressBar.setVisibility(View.GONE));
                        }
                    }, 500);
                }
            });
        }

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, getActivity().getResources().getDisplayMetrics());
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private Dialog createDisabledUserDialog(int userId, FormattedEntity entity) {

        final String userName = entity.getName();
        final String userDivision = entity.getUserDivision();
        final String userPosition = entity.getUserPosition();
        final String userProfileUrl = entity.getUserLargeProfileUrl();
        final boolean isStarred = entity.isStarred;

        View mainView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_disable_user_profile, null);

        final ImageView imgUserPhoto = (ImageView) mainView.findViewById(R.id.img_user_info_photo);
        imgStarred = (ImageView) mainView.findViewById(R.id.img_user_info_starred);
        final TextView txtUserName = (TextView) mainView.findViewById(R.id.txt_user_info_name);
        final TextView txtUserDivision = (TextView) mainView.findViewById(R.id.txt_user_info_division);
        final TextView txtUserPosition = (TextView) mainView.findViewById(R.id.txt_user_info_position);

        setProfileStarred(isStarred);
        txtUserName.setText(userName);
        txtUserDivision.setText(userDivision);
        txtUserPosition.setText(userPosition);
        Ion.with(imgUserPhoto)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(userProfileUrl);

        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, getActivity().getResources().getDisplayMetrics());
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @UiThread
    void setProfileStarred(boolean isStarred) {
        if (isStarred) {
            imgStarred.setImageResource(R.drawable.profile_fav_on);
        } else {
            imgStarred.setImageResource(R.drawable.profile_fav_off);
        }
    }

    @Background
    void onStarClick(int entityId) {

        FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);

        try {
            if (entity.isStarred) {
                RequestManager.newInstance(getActivity(), () -> jandiEntityClient.disableFavorite(entityId)).request();
            } else {
                RequestManager.newInstance(getActivity(), () -> jandiEntityClient.enableFavorite(entityId)).request();
            }


            entity.isStarred = !entity.isStarred;

            setProfileStarred(entity.isStarred);

            EventBus.getDefault().post(new RetrieveTopicListEvent());


        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }

    }

}
