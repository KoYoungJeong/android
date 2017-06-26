package com.tosslab.jandi.app.ui.push;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

/**
 * Created by tee on 2017. 6. 12..
 */

public class PushPopupActivity extends AppCompatActivity {

    @InjectExtra
    @Nullable
    String profileUrl;
    @InjectExtra
    String memberName;
    @InjectExtra
    String roomName;
    @InjectExtra
    String message;
    @InjectExtra
    long roomId;
    @InjectExtra
    int roomTypeInt;
    @InjectExtra
    long teamId;
    @InjectExtra
    String roomType;

    @Bind(R.id.iv_member_profile)
    ImageView ivMemberProfile;
    @Bind(R.id.vg_message_info)
    LinearLayout vgMessageInfo;
    @Bind(R.id.tv_member_name)
    TextView tvMemberName;
    @Bind(R.id.tv_room_name)
    TextView tvRoomName;
    @Bind(R.id.tv_push_message)
    TextView tvPushMessage;

    private boolean isRunning;

    public static void startActivity(Context context,
                                     String profileUrl,
                                     String memberName,
                                     String roomName,
                                     String message,
                                     long teamId,
                                     long roomId,
                                     int roomTypeInt,
                                     String roomType) {
        context.startActivity(Henson.with(context)
                .gotoPushPopupActivity()
                .memberName(memberName)
                .message(message)
                .roomId(roomId)
                .roomName(roomName)
                .roomType(roomType)
                .roomTypeInt(roomTypeInt)
                .teamId(teamId)
                .profileUrl(profileUrl)
                .build().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
        Observable.just(1)
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    if (isRunning) {
                        JandiApplication.setIsPushPopupActivityActive(true);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        JandiApplication.setIsPushPopupActivityActive(false);
        Observable.just(1)
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    if (!isRunning) {
                        finish();
                    }
                });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_popup);
        ButterKnife.bind(this);
        Dart.inject(this);
        initPopupViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Dart.inject(this, intent.getExtras());
        initPopupViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JandiApplication.setIsPushPopupActivityActive(false);
        Observable.just(1)
                .delay(1500, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    JandiApplication.setIsApplicationDeactive(true);
                });
    }

    private void initPopupViews() {
        if (!TextUtils.isEmpty(profileUrl)) {
            ImageUtil.loadProfileImage(ivMemberProfile, profileUrl, R.drawable.profile_img);
        }

        tvMemberName.setText(memberName);

        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(tvMemberName.getTypeface());
        float textSize = tvMemberName.getTextSize();
        paint.setTextSize(textSize);
        String text = tvMemberName.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int memberInfoLeftMargin = (int) UiUtils.getPixelFromDp(92);
        int memberInfoRightMargin = (int) UiUtils.getPixelFromDp(67);
        int memberInfoWith = displayWidth - memberInfoLeftMargin - memberInfoRightMargin;

        int maxNameWidth = (memberInfoWith * 65) / 100; // memberInfo width의 65 %
        LinearLayout.LayoutParams nameLayoutParams =
                (LinearLayout.LayoutParams) tvMemberName.getLayoutParams();
        if (bounds.width() > maxNameWidth) {
            nameLayoutParams.width = maxNameWidth;
            tvMemberName.setLayoutParams(nameLayoutParams);
        } else {
            nameLayoutParams.width = bounds.width() + 5;
            tvMemberName.setLayoutParams(nameLayoutParams);
        }

        tvRoomName.setText("[" + roomName + "]");
        tvPushMessage.setText(getOutMessage(roomTypeInt, message));
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @OnClick(R.id.iv_close_button)
    void onClickCloseButton() {
        finish();
    }

    @OnClick(R.id.vg_popup)
    void onClickPopup() {
        Intent intent =
                PushInterfaceActivity.getIntent(this, roomId, roomTypeInt, true, teamId, roomType);
        startActivity(intent);
        finish();
    }

    private String getOutMessage(int roomTypeInt, String message) {
        String pushPreviewInfo = JandiPreference.getPushPreviewInfo();
        String outMessage;
        if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_PREVIEW_ALL_MESSAGE)) {
            outMessage = message;
        } else if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_PREVIEW_PUBLIC_ONLY)) {
            if (roomTypeInt == JandiConstants.TYPE_PUBLIC_TOPIC) {
                outMessage = message;
            } else {
                outMessage = JandiApplication.getContext().getString(R.string.jandi_no_preview_push_message);
            }
        } else if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_NO_PREVIEW)) {
            outMessage = JandiApplication.getContext().getString(R.string.jandi_no_preview_push_message);
        } else {
            outMessage = message;
        }
        return outMessage;
    }

}
