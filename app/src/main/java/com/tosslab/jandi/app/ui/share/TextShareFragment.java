package com.tosslab.jandi.app.ui.share;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.share.presenter.text.TextSharePresenter;
import com.tosslab.jandi.app.ui.share.presenter.text.TextSharePresenterImpl;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@EFragment(R.layout.fragment_share_text)
public class TextShareFragment extends Fragment implements MainShareActivity.Share, TextSharePresenter.View {


    @FragmentArg
    String subject;

    @FragmentArg
    String text;

    @ViewById(R.id.et_share_comment)
    EditText etComment;

    @ViewById(R.id.progress_share_image)
    ProgressBar downloadingProgressBar;

    @ViewById(R.id.tv_room_name)
    TextView tvRoomName;

    @ViewById(R.id.tv_team_name)
    TextView tvTeamName;

    @Bean(TextSharePresenterImpl.class)
    TextSharePresenter textSharePresenterImpl;

    MentionControlViewModel mentionControlViewModel;

    @AfterInject
    void initObject() {
        textSharePresenterImpl.setView(this);
    }

    @AfterViews
    void initViews() {
        StringBuffer buffer = new StringBuffer();
        if (!TextUtils.isEmpty(subject)) {
            buffer.append(subject).append("\n");
        }

        if (!TextUtils.isEmpty(text)) {
            buffer.append(text);
        }

        etComment.setText(buffer.toString());
        etComment.setSelection(etComment.getText().length());
        etComment.setMaxLines(6);

        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });
        textSharePresenterImpl.initViews();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }


    @Override
    public void onPause() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Click(R.id.vg_team)
    void clickSelectTeam() {
        LogUtil.e("team");
        ShareSelectTeamActivity_
                .intent(this)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @Click(R.id.vg_room)
    void clickSelectRoom() {
        LogUtil.e("room");
        ShareSelectRoomActivity_
                .intent(this)
                .extra("teamId", textSharePresenterImpl.getTeamId())
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @Click(R.id.et_share_comment)
    void clickComment() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TapComment);
    }

    public void onEvent(ShareSelectTeamEvent event) {
        int teamId = event.getTeamId();
        textSharePresenterImpl.initEntityData(teamId);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        int roomId = event.getRoomId();
        textSharePresenterImpl.setEntity(roomId);
    }


    @Override
    public void startShare() {
        List<MentionObject> mentions;
        String messageText;
        if (mentionControlViewModel != null) {
            ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject();
            mentions = mentionInfoObject.getMentions();
            messageText = mentionInfoObject.getMessage();
        } else {
            mentions = new ArrayList<>();
            messageText = etComment.getText().toString();
        }

        textSharePresenterImpl.sendMessage(messageText, mentions);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishOnUiThread() {
        getActivity().finish();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveIntro() {
        IntroActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        finishOnUiThread();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTeamName(String teamName) {
        tvTeamName.setText(teamName);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setRoomName(String roomName) {
        tvRoomName.setText(roomName);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setMentionInfo(int teamId, int roomId, int roomType) {
        mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(), etComment,
                teamId,
                Arrays.asList(roomId),
                MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressBar() {
        downloadingProgressBar.setVisibility(View.VISIBLE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressBar() {
        downloadingProgressBar.setVisibility(View.GONE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveEntity(int teamId, int roomId, int roomType) {
        MainTabActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .roomId(roomType != JandiConstants.TYPE_DIRECT_MESSAGE ? roomId : -1)
                .entityId(roomId)
                .entityType(roomType)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1, 1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                });

    }


}
