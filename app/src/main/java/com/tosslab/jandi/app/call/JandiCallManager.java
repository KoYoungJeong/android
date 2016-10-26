package com.tosslab.jandi.app.call;


import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class JandiCallManager {
    private static final String TAG = "JandiCallManager";
    private static JandiCallManager instance;

    private PublishSubject<CallState> subject;

    private JandiCallManager() {
        subject = PublishSubject.create();

        subject.onBackpressureBuffer()
                .concatMap(callState -> {
                    Observable<CallState> initObservable = Observable.just(callState);
                    switch (callState.state) {
                        case TelephonyManager.CALL_STATE_RINGING:   // 전화 옴
                            LogUtil.d(TAG, "extractNetworkPortion : " + PhoneNumberUtils.extractNetworkPortion(callState.number));
                            LogUtil.d(TAG, "extractPostDialPortion : " + PhoneNumberUtils.extractPostDialPortion(callState.number));
                            return initObservable.compose(this::ringingState).concatMap(it -> Observable.just(callState));
                        case TelephonyManager.CALL_STATE_OFFHOOK:   // 전화 받음
                            return initObservable.compose(this::offHookState).concatMap(it -> Observable.just(callState));
                        case TelephonyManager.CALL_STATE_IDLE:      // 전화 끊음
                            return initObservable.compose(this::idleState).concatMap(it -> Observable.just(callState));
                    }
                    return Observable.just(callState);
                })
                .subscribe(it -> {}, Crashlytics::logException);
    }

    private Observable<CallState> offHookState(Observable<CallState> callState) {
        return callState;
    }

    private Observable<Object> ringingState(Observable<CallState> callStateObservable) {
        return callStateObservable
                .filter(it -> {
                    if (!TextUtils.isEmpty(it.number)) {
                        return false;
                    }

                    String queryNum;
                    if (it.number.length() >= 3) {
                        queryNum = it.number.substring(it.number.length() - 3, it.number.length());
                    } else {
                        queryNum = it.number;
                    }

                    boolean containsPhone = HumanRepository.getInstance().containsPhone(queryNum);
                    LogUtil.d(TAG, "containsPhone : " + containsPhone);
                    return containsPhone;
                }).concatMap(callState -> {
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

                        boolean compare = PhoneNumberUtils.compare(JandiApplication.getContext(), number, plainPhoneNumber);
                        LogUtil.d(TAG, "call Number : " + number + ", queried Number : " + plainPhoneNumber + ", comapre : " + compare);
                        return compare;
                    });
                });
    }

    private Observable<CallState> idleState(Observable<CallState> callState) {
        return callState;
    }

    synchronized public static JandiCallManager getInstance() {
        if (instance == null) {
            instance = new JandiCallManager();
        }
        return instance;
    }

    public void onCall(String inComingNumber, int state) {

        subject.onNext(CallState.create(inComingNumber, state));
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
}
