package com.tosslab.jandi.lib.sprinkler.track.factory;

import android.text.TextUtils;

import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.track.FutureTrack;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class AccountTrackFactory {
    public static FutureTrack getSignInTrack(boolean success,
                                             String accountId, String memberId,
                                             int errorCode) {
        FutureTrack.Builder builder = new FutureTrack.Builder();
        builder.event(Event.SignIn);
        builder.accountId(accountId);
        builder.memberId(memberId);
        builder.property(PropertyKey.ResponseSuccess, success);
        if (success) {
            builder.property(PropertyKey.AutoSignIn, !TextUtils.isEmpty(memberId));
        } else {
            builder.property(PropertyKey.ErrorCode, errorCode);
        }
        return builder.build();
    }

    public static FutureTrack getSendVerificationEmailTrack(boolean success, String email,
                                                            int errorCode) {
        FutureTrack.Builder builder = new FutureTrack.Builder();
        builder.event(Event.SendAccountVerificationMail);
        builder.property(PropertyKey.ResponseSuccess, success);
        if (success) {
            builder.property(PropertyKey.Email, email);
        } else {
            builder.property(PropertyKey.ErrorCode, errorCode);
        }
        return builder.build();
    }

    public static FutureTrack getResendVerificationEmailTrack(boolean success, String email,
                                                              int errorCode) {
        FutureTrack.Builder builder = new FutureTrack.Builder();
        builder.event(Event.ResendAccountVerificationMail);
        builder.property(PropertyKey.ResponseSuccess, success);
        if (success) {
            builder.property(PropertyKey.Email, email);
        } else {
            builder.property(PropertyKey.ErrorCode, errorCode);
        }
        return builder.build();
    }

    public static FutureTrack getSignUpTrack(boolean success, String accountId, int errorCode) {
        FutureTrack.Builder builder = new FutureTrack.Builder();
        builder.event(Event.SignOut);
        builder.property(PropertyKey.ResponseSuccess, success);
        if (success) {
            builder.accountId(accountId);
        } else {
            builder.property(PropertyKey.ErrorCode, errorCode);
        }
        return builder.build();
    }

    public static FutureTrack getSignOutTrack(String accountId, String memberId) {
        return new FutureTrack.Builder()
                .event(Event.SignOut)
                .accountId(accountId)
                .memberId(memberId)
                .build();
    }

    public static FutureTrack getLaunchTeamTrack(boolean success, String accountId, String teamId,
                                                 int errorCode) {
        FutureTrack.Builder builder = new FutureTrack.Builder();
        builder.event(Event.LaunchTeam);
        builder.property(PropertyKey.ResponseSuccess, success);
        if (success) {
            builder.property(PropertyKey.TeamId, teamId);
        } else {
            builder.property(PropertyKey.ErrorCode, errorCode);
        }
        return builder.build();
    }

}
