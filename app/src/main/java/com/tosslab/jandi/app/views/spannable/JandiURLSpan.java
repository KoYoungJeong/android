package com.tosslab.jandi.app.views.spannable;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.network.client.conference_call.ConferenceCallApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.GooroomeeRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqGooroomeeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeeOtp;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 21..
 */
public class JandiURLSpan extends UnderlineSpan implements ClickableSpannable {
    private final Context context;
    private final String url;
    private final int color;

    public JandiURLSpan(Context context, String url, int color) {
        this.context = context;
        this.url = url;
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }

    @Override
    public void onClick() {
        if (isGooroomeeLink()) {
            Observable.defer(() -> {
                ConferenceCallApi conferenceCallApi =
                        new ConferenceCallApi(GooroomeeRetrofitBuilder.getInstance());
                ReqGooroomeeOtp reqGooroomeeOtp = new ReqGooroomeeOtp();
                reqGooroomeeOtp.roomId = getGooroomeeRoomId();
                TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
                long myId = teamInfoLoader.getMyId();
                reqGooroomeeOtp.userName = teamInfoLoader.getUser(myId).getName();
                Level myLevel = teamInfoLoader.getMyLevel();
                reqGooroomeeOtp.roleId = "emcee";
                if (myLevel == Level.Guest) {
                    reqGooroomeeOtp.roleId = "participant";
                }
                ResGooroomeeOtp resGooroomeeOtp = null;
                try {
                    resGooroomeeOtp = conferenceCallApi.getGooroomeOtp(reqGooroomeeOtp);
                } catch (RetrofitException e) {
                    e.printStackTrace();
                }
                return Observable.just(resGooroomeeOtp);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resGooroomeeOtp -> {
                        if (resGooroomeeOtp != null) {
                            if (resGooroomeeOtp.resultCode.equals("GRM_700")) {
                                //토스트 메세지 띄워주기
                            } else if (resGooroomeeOtp.data != null &&
                                    resGooroomeeOtp.data.roomUserOtp != null) {
                                try {
                                    String uriScheme = "https://gooroomee.com/room/otp/"
                                            + resGooroomeeOtp.data.roomUserOtp.otp;
                                    Uri uri = Uri.parse(uriScheme);
                                    Intent intent = new Intent(Intent.ACTION_VIEW)
                                            .setData(uri)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .setPackage("com.android.chrome");
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    try {
                                        context.startActivity(
                                                new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("market://details?id=com.android.chrome")));
                                    } catch (ActivityNotFoundException e1) {
                                        context.startActivity(
                                                new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("https://play.google.com/store/apps/details?id=com.android.chrome")));
                                    }
                                }
                            } else {
                                //토스트 메세지 띄워주기
                            }
                        }
                    });
        } else {
            ApplicationUtil.startWebBrowser(context, url);
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.MsgURL);
    }

    public boolean isGooroomeeLink() {
        return url.contains("jandiapp://GOOROOMEE?");
    }

    public String getGooroomeeRoomId() {
        return url.replace("jandiapp://GOOROOMEE?roomId=", "");
    }
}