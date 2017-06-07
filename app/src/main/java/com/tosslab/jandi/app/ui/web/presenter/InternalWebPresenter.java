package com.tosslab.jandi.app.ui.web.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.messages.ReqTextMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import java.net.URISyntaxException;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class InternalWebPresenter {

    private View view;

    public void setView(View view) {
        this.view = view;
    }

    public void zendeskCookieRemove(Context context) {
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
    }

    public String createMessage(String title, String url) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(title).append("\n").append(url);
        return buffer.toString();
    }


    public void sendMessageToRoom(long entityId, String text) throws RetrofitException {
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        RoomsApi roomsAPi = new RoomsApi(InnerApiRetrofitBuilder.getInstance());

        ReqTextMessage reqMessage = new ReqTextMessage(text, null);

        roomsAPi.sendMessage(teamId, entityId, reqMessage);
    }

    public String getAvailableUrl(String url) {
        String urlLowerCase = url.toLowerCase();

        if (!urlLowerCase.startsWith("http")) {
            url = "http://" + url;
        }

        return url;
    }

    public void sendMessage(Activity activity, String title, String Url, ShareSelectRoomEvent event) {
        Context context = JandiApplication.getContext();
        view.showProgressWheel();
        Completable.fromCallable(() -> {

            long entityId = event.getRoomId();
            String message = createMessage(title, Url);
            sendMessageToRoom(entityId, message);
            return true;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgressWheel();

                    view.showSuccessToast(context, context.getString(R.string.jandi_share_succeed,
                            context.getString(R.string.jandi_message_hint)));
                }, t -> {
                    t.printStackTrace();
                    view.dismissProgressWheel();
                    view.showErrorToast(context, context.getString(R.string.err_network));

                });
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!url.startsWith("http")) {
            if (url.startsWith("intent")) {
                Intent intent;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
                return InternalWebPresenter.this.view.launchNewBrowser(intent.getDataString());
            } else {
                return InternalWebPresenter.this.view.launchNewBrowser(url);
            }
        }
        return false;
    }

    public interface View {
        void setActionBarTitle(String title);

        void showSuccessToast(Context context, String message);

        void showErrorToast(Context context, String message);

        void LaunchPageNotFoundActivity();

        void setWebLoadingProgress(int newProgress);

        boolean launchNewBrowser(String url);

        void showProgressWheel();

        void dismissProgressWheel();

        void loadWebPage(WebView webView, String url);
    }

}
