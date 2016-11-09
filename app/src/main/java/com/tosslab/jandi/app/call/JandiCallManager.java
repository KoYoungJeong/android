package com.tosslab.jandi.app.call;


import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.rxrelay.PublishRelay;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class JandiCallManager {
    public static final String[] CONTACTS_PROJECTION = new String[]{ContactsContract.Contacts._ID};
    private static JandiCallManager instance;
    private final PhoneNumberUtil phoneNumberUtil;

    private PublishRelay<CallState> subject;
    private View view;
    private WindowManager.LayoutParams params;

    private JandiCallManager() {

        phoneNumberUtil = PhoneNumberUtil.createInstance(JandiApplication.getContext());

        subject = PublishRelay.create();

        subject.onBackpressureBuffer()
                .filter(it -> {
                    if (SdkUtils.isMarshmallow()) {
                        if (Settings.canDrawOverlays(JandiApplication.getContext())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                })
                .concatMap(callState -> {
                    Observable<CallState> initObservable = Observable.just(callState);
                    switch (callState.state) {
                        case TelephonyManager.CALL_STATE_RINGING:   // 전화 옴
                            return initObservable.compose(this::ringingState).concatMap(it -> Observable.just(callState));
                        case TelephonyManager.CALL_STATE_OFFHOOK:   // 전화 받음
                            return initObservable.compose(this::offHookState).concatMap(it -> Observable.just(callState));
                        case TelephonyManager.CALL_STATE_IDLE:      // 전화 끊음
                            return initObservable.compose(this::idleState).concatMap(it -> Observable.just(callState));
                    }
                    return Observable.just(callState);
                })
                .subscribe(it -> {}, (throwable) -> {
                    throwable.printStackTrace();
                    Crashlytics.logException(throwable);
                });
    }

    synchronized public static JandiCallManager getInstance() {
        if (instance == null) {
            instance = new JandiCallManager();
        }
        return instance;
    }

    private Observable<CallState> offHookState(Observable<CallState> callState) {
        return callState;
    }

    private Observable<? extends Object> ringingState(Observable<CallState> callStateObservable) {
        return callStateObservable
                .filter(it -> {
                    if (SdkUtils.isMarshmallow()) {
                        return Settings.canDrawOverlays(JandiApplication.getContext());
                    } else {
                        return false;
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .filter(callState -> {

                    if (SdkUtils.hasPermission(JandiApplication.getContext(), Manifest.permission.CALL_PHONE)) {
                        Cursor cursor = JandiApplication.getContext()
                                .getContentResolver()
                                .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(callState.number)),
                                        CONTACTS_PROJECTION,
                                        null,
                                        null,
                                        null);
                        if (cursor != null) {
                            try {
                                return !(cursor.getCount() > 0);
                            } finally {
                                cursor.close();
                            }
                        }
                    } else {
                        return true;
                    }

                    return true;
                })
                .filter(it -> {
                    if (TextUtils.isEmpty(it.number)) {
                        return false;
                    }

                    String queryNum;
                    if (it.number.length() >= 3) {
                        queryNum = it.number.substring(it.number.length() - 3, it.number.length());
                    } else {
                        queryNum = it.number;
                    }

                    return HumanRepository.getInstance().containsPhone(queryNum);
                })
                .concatMap(callState -> {
                    String queryNum;
                    String number = callState.number.replaceAll("[^0-9]", "");
                    if (number.length() >= 3) {
                        queryNum = number.substring(number.length() - 3, number.length());
                    } else {
                        queryNum = number;
                    }
                    List<Human> containsPhone = HumanRepository.getInstance().getContainsPhone(queryNum);

                    if (containsPhone == null) {
                        return Observable.empty();
                    }

                    return Observable.from(containsPhone).map(User::new).takeFirst(user -> {
                        String phoneNumber = user.getPhoneNumber();
                        String plainPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

                        PhoneNumberUtil.MatchType numberMatch = phoneNumberUtil.isNumberMatch(number, plainPhoneNumber);
                        return numberMatch.ordinal() > PhoneNumberUtil.MatchType.NO_MATCH.ordinal();
                    });
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> {
                    WindowManager windowManager = (WindowManager) JandiApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
                    if (view == null) {
                        if (params == null) {
                            params = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,//항상 최 상위. 터치 이벤트 받을 수 있음.
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //포커스를 가지지 않음
                                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                                    PixelFormat.TRANSLUCENT); // 투명
                            params.gravity = Gravity.CENTER;
                            params.y = JandiPreference.getCallPreviewCoordinateY();
                        }
                        view = LayoutInflater.from(JandiApplication.getContext()).inflate(R.layout.view_call_preview, null);
                        windowManager.addView(view, params);

                        view.setOnTouchListener(new View.OnTouchListener() {
                            private int y;

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        y = (int) event.getRawY();
                                        return true;
                                    case MotionEvent.ACTION_MOVE:
                                        if (view != null) {
                                            params.y += ((int) event.getRawY() - y);
                                            windowManager.updateViewLayout(view, params);
                                        }
                                        y = (int) event.getRawY();
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                        JandiPreference.setCallPreviewCoordinateY(params.y);
                                        return false;
                                }
                                return false;
                            }
                        });
                    }

                    new ProfileView(view).bind(it, this::removeView);
                });
    }

    private Observable<CallState> idleState(Observable<CallState> callState) {
        return callState.observeOn(AndroidSchedulers.mainThread()).doOnNext(it -> removeView());
    }

    synchronized private void removeView() {
        if (view != null) {
            WindowManager windowManager = (WindowManager) JandiApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeViewImmediate(view);
            view = null;
        }
    }

    public void onCall(String inComingNumber, int state) {

        subject.call(CallState.create(inComingNumber, state));
    }

    private static class CallState {
        String number;
        int state;

        private CallState(String number, int state) {
            this.number = number;
            this.state = state;
        }

        public static CallState create(String number, int state) {
            return new CallState(number, state);
        }
    }

    static class ProfileView {

        @Bind(R.id.tv_call_preview_tel)
        TextView tvTel;
        @Bind(R.id.tv_call_preview_name)
        TextView tvName;
        @Bind(R.id.tv_call_preview_dept)
        TextView tvDept;
        @Bind(R.id.tv_call_preview_position)
        TextView tvPosition;

        @Bind(R.id.iv_call_preview_thumb)
        ImageView ivProfile;

        @Bind(R.id.btn_call_preview_close)
        View btnClose;

        public ProfileView(View itemView) {
            ButterKnife.bind(this, itemView);
        }

        void bind(User user, Runnable closeListener) {
            tvTel.setText(user.getPhoneNumber());
            tvName.setText(user.getName());
            if (!TextUtils.isEmpty(user.getDivision())) {
                tvDept.setText(user.getDivision());
                tvDept.setVisibility(View.VISIBLE);
            } else {
                tvDept.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(user.getPosition())) {
                tvPosition.setText(user.getPosition());
                tvPosition.setVisibility(View.VISIBLE);
            } else {
                tvPosition.setVisibility(View.GONE);
            }

            ImageUtil.loadProfileImage(ivProfile, user.getPhotoUrl(), R.drawable.profile_img);

            btnClose.setOnClickListener(v -> {
                if (closeListener != null) {
                    closeListener.run();
                }
            });
        }
    }
}
