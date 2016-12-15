package com.tosslab.jandi.app.ui.share.text;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.share.MainShareActivity;
import com.tosslab.jandi.app.ui.share.text.dagger.DaggerTextShareComponent;
import com.tosslab.jandi.app.ui.share.text.dagger.TextShareModule;
import com.tosslab.jandi.app.ui.share.text.presenter.TextSharePresenter;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class TextShareFragment extends Fragment implements MainShareActivity.Share, TextSharePresenter.View {

    @Nullable
    @InjectExtra
    String subject;

    @InjectExtra
    String text;

    @Bind(R.id.et_share_comment)
    EditText etComment;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.tv_team_name)
    TextView tvTeamName;

    @Inject
    TextSharePresenter textSharePresenterImpl;

    MentionControlViewModel mentionControlViewModel;

    ProgressWheel progressWheel;

    public static TextShareFragment create(Context context, String subject, String text) {
        Bundle bundle = new Bundle();
        bundle.putString("subject", subject);
        bundle.putString("text", text);
        return (TextShareFragment) Fragment.instantiate(context, TextShareFragment.class.getName(), bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_text, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }

        DaggerTextShareComponent.builder()
                .textShareModule(new TextShareModule(this))
                .build()
                .inject(this);
        initProgressWheel();
        initViews();
        setHasOptionsMenu(true);

    }

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

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(getActivity());
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_share);
            if (item != null) {
                item.setEnabled(etComment.length() > 0 && tvRoomName.length() > 0);
            }
        }
    }

    @OnTextChanged(R.id.et_share_comment)
    void onCommentTextChanged(CharSequence text) {
        getActivity().supportInvalidateOptionsMenu();
    }

    @OnClick(R.id.vg_team)
    void clickSelectTeam() {
        LogUtil.e("team");
        startActivity(new Intent(getActivity(), ShareSelectTeamActivity.class));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @OnClick(R.id.vg_room)
    void clickSelectRoom() {
        LogUtil.e("room");
        startActivity(Henson                .with(getActivity())
                .gotoShareSelectRoomActivity()
                .teamId(textSharePresenterImpl.getTeamId())
                .build());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @OnClick(R.id.et_share_comment)
    void clickComment() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TapComment);
    }

    public void onEvent(ShareSelectTeamEvent event) {
        long teamId = event.getTeamId();
        textSharePresenterImpl.initEntityData(teamId);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        long roomId = event.getRoomId();
        int roomType = event.getRoomType();
        textSharePresenterImpl.setEntity(roomId, roomType);
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
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void finishOnUiThread() {
        getActivity().finish();
    }

    @Override
    public void setTeamName(String teamName) {
        tvTeamName.setText(teamName);
    }

    @Override
    public void setRoomName(String roomName) {
        tvRoomName.setText(roomName);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void setMentionInfo(long teamId, long roomId, int roomType) {
        mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(), etComment,
                teamId,
                Arrays.asList(roomId),
                MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
    }

    @Override
    public void showProgressBar() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void dismissProgressBar() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void moveEntity(long teamId, long roomId, long entityId, int roomType) {

        Completable.fromAction(() -> {
            if (getActivity() == null) {
                return;
            }
            UploadNotificationActivity.startActivity(getActivity(), teamId, entityId);
            getActivity().finish();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
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

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (mentionControlViewModel != null) {
            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(event.getId());
            searchedItemVO.setName(event.getName());
            searchedItemVO.setType(event.getType());
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }
}
