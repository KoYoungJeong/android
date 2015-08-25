package com.tosslab.jandi.app.dialogs.profile;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by justinygchoi on 2014. 9. 3..
 */
@EFragment
public class UserInfoDialogFragment extends DialogFragment {

    @FragmentArg
    int entityId;

    @Bean
    EntityClientManager entityClientManager;
    private ImageView imgStarred;

    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        if (TextUtils.equals(entity.getUser().status, "enabled")) {
            return createEnabledUserDialog();
        } else {
            return createDisabledUserDialog();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        if (TextUtils.equals(entity.getUser().status, "enabled")) {
            setUpEnabledProfile(entity);
        } else {
            setUpDisabledProfile(entity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEvent(MemberStarredEvent event) {
        if (imgStarred == null || event.getId() != entityId) {
            return;
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        setProfileStarred(entity.isStarred);

    }

    public void onEvent(ProfileChangeEvent event) {
        if (event.getMember().id == entityId) {
            FormattedEntity entityById =
                    EntityManager.getInstance().getEntityById(entityId);

            setUpEnabledProfile(entityById);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setUpEnabledProfile(FormattedEntity entity) {
        final String userName = entity.getName();
        final String userStatusMessage = entity.getUserStatusMessage();
        final String userDivision = entity.getUserDivision();
        final String userPosition = entity.getUserPosition();
        final String userPhoneNumber = entity.getUserPhoneNumber();
        final String userEmail = entity.getUserEmail();
        final String userProfileUrl = entity.getUserLargeProfileUrl();
        final boolean isMe = EntityManager.getInstance().isMe(entityId);
        final boolean isStarred = entity.isStarred;


        View mainView = getDialog().getWindow().getDecorView();

        final ImageView imgUserPhoto = (ImageView) mainView.findViewById(R.id.img_user_info_photo);
        imgStarred = (ImageView) mainView.findViewById(R.id.img_user_info_starred);
        final TextView txtUserName = (TextView) mainView.findViewById(R.id.txt_user_info_name);
        final TextView txtUserStatusMessage =
                (TextView) mainView.findViewById(R.id.txt_user_info_statusmessage);
        final TextView txtUserDivision =
                (TextView) mainView.findViewById(R.id.txt_user_info_division);
        final TextView txtUserPosition =
                (TextView) mainView.findViewById(R.id.txt_user_info_position);
        final TextView txtUserEmail = (TextView) mainView.findViewById(R.id.txt_user_info_email);
        final TextView txtUserPhone = (TextView) mainView.findViewById(R.id.txt_user_info_phone);
        final LinearLayout lyUserEmail = (LinearLayout) mainView.findViewById(R.id.ly_user_info_mail);
        final LinearLayout lyUserPhone = (LinearLayout) mainView.findViewById(R.id.ly_user_info_phone);
        final LinearLayout lyUserDirectMessage =
                (LinearLayout) mainView.findViewById(R.id.ly_user_info_direct_message);
        final View borderUserDirectMessage =
                mainView.findViewById(R.id.border_user_info_direct_message);

        if (!isMe) {
            setProfileStarred(isStarred);
        } else {
            imgStarred.setVisibility(View.GONE);
        }

        imgStarred.setOnClickListener(v -> onStarClick(entityId));

        if (isMe) {     // 본인의 정보면 1:1 대화 버튼을 보여주지 않는다.
            lyUserDirectMessage.setVisibility(View.GONE);
            borderUserDirectMessage.setVisibility(View.GONE);
        } else {
            lyUserDirectMessage.setOnClickListener(view -> {
                EventBus.getDefault().post(new RequestMoveDirectMessageEvent(entityId));
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
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
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
        txtUserStatusMessage.setText(userStatusMessage);
        txtUserDivision.setText(userDivision);
        txtUserPosition.setText(userPosition);
        Ion.with(imgUserPhoto)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(userProfileUrl);

        LogUtil.i("user profile url = " + userProfileUrl);
        Log.i("JANDI", "user profile url = " + userProfileUrl);
        if (!hasChangedProfileUrl(userProfileUrl)) {
            return;
        }

        imgUserPhoto.setOnClickListener(v -> {
            Dialog alertDialog = new Dialog(getActivity());
            View view =
                    LayoutInflater.from(getActivity()).inflate(R.layout.dialog_profile_view, null);
            PhotoView profileView = (PhotoView) view.findViewById(R.id.photo_dialog_profile_view);
            ProgressBar progressBar =
                    (ProgressBar) view.findViewById(R.id.progress_dialog_profile_view);

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

            new Handler().postDelayed(() ->
                    Ion.with(profileView)
                            .deepZoom()
                            .load(userProfileUrl)
                            .setCallback((e, result) -> progressBar.setVisibility(View.GONE)), 500);
        });
    }

    // TODO Profile Image 를 수정 했는지에 대한 판단이 명확해질 필요가 있음.
    private boolean hasChangedProfileUrl(String url) {
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    private Dialog createEnabledUserDialog() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_user_profile, null);

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 260, getActivity().getResources().getDisplayMetrics());
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private Dialog createDisabledUserDialog() {
        View mainView =
                LayoutInflater.from(getActivity()).inflate(R.layout.dialog_disable_user_profile, null);

        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 260, getActivity().getResources().getDisplayMetrics());
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private View setUpDisabledProfile(FormattedEntity entity) {
        final String userName = entity.getName();
        final String userDivision = entity.getUserDivision();
        final String userPosition = entity.getUserPosition();
        final String userProfileUrl = entity.getUserLargeProfileUrl();
        final boolean isStarred = entity.isStarred;

        View mainView = getDialog().getWindow().getDecorView();

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
        return mainView;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setProfileStarred(boolean isStarred) {
        if (isStarred) {
            imgStarred.setImageResource(R.drawable.profile_fav_on);
        } else {
            imgStarred.setImageResource(R.drawable.profile_fav_off);
        }
    }

    @Background
    void onStarClick(int entityId) {

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        try {
            if (entity.isStarred) {
                entityClientManager.disableFavorite(entityId);
            } else {
                entityClientManager.enableFavorite(entityId);
            }

            entity.isStarred = !entity.isStarred;

            setProfileStarred(entity.isStarred);

            EventBus.getDefault().post(new RetrieveTopicListEvent());

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
