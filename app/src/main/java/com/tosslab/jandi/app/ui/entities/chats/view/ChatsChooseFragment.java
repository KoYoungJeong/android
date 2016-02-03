package com.tosslab.jandi.app.ui.entities.chats.view;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenter;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenterImpl;
import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
@EFragment(R.layout.fragment_chat_choose)
public class ChatsChooseFragment extends Fragment implements ChatChoosePresenter.View {

    private static final int REQ_DISABLED_MEMBERS = 901;
    public static final String EXTRA_ENTITY_ID = "entity_id";
    @ViewById(R.id.list_chat_choose)
    ListView lvChatChoose;

    @ViewById(R.id.layout_member_empty)
    View emptyMemberView;
    ChatChooseAdapter chatChooseAdapter;
    @SystemService
    ClipboardManager clipboardManager;
    @SystemService
    InputMethodManager inputMethodManager;

    @Bean(ChatChoosePresenterImpl.class)
    ChatChoosePresenter presenter;

    @AfterInject
    void initObjects() {
        presenter.setView(this);
    }

    @AfterViews
    void initViews() {
        chatChooseAdapter = new ChatChooseAdapter(getActivity());
        chatChooseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (chatChooseAdapter.isEmpty()) {
                    emptyMemberView.setVisibility(View.VISIBLE);
                } else {
                    emptyMemberView.setVisibility(View.GONE);
                }
            }
        });

        lvChatChoose.setAdapter(chatChooseAdapter);

        presenter.initMembers();

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUsers(List<ChatChooseItem> users) {
        chatChooseAdapter.clear();
        chatChooseAdapter.addAll(users);
        chatChooseAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveChatMessage(long teamId, long entityId) {
        getActivity().finish();
        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(-1)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }


    public void onEventMainThread(ShowProfileEvent event) {
        MemberProfileActivity_.intent(getActivity())
                .memberId(event.userId)
                .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();

        AnalyticsValue.Action action = AnalyticsUtil.getProfileAction(event.userId, event.from);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamMembers, action);
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        presenter.onMoveChatMessage(event.userId);
    }

    @ItemClick(R.id.list_chat_choose)
    void onEntitySelect(int position) {
        ChatChooseItem chatChooseItem = chatChooseAdapter.getItem(position);
        if (chatChooseItem instanceof DisableDummyItem) {
            DisabledEntityChooseActivity_.intent(ChatsChooseFragment.this)
                    .startForResult(REQ_DISABLED_MEMBERS);
        } else {
            presenter.onMoveChatMessage(chatChooseItem.getEntityId());
        }
    }

    @OnActivityResult(REQ_DISABLED_MEMBERS)
    void onDisabledMemberActivityResult(int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (data.hasExtra(EXTRA_ENTITY_ID)) {
            presenter.onMoveChatMessage(data.getLongExtra(EXTRA_ENTITY_ID, -1));
        }
    }

    @EditorAction(R.id.et_chat_choose_search)
    void onSearchTextImeAction(TextView textView) {
        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
    }

    @TextChange(R.id.et_chat_choose_search)
    void onSearchTextChange(CharSequence text) {
        presenter.onSearch(text.toString());
    }

    @Click(R.id.btn_chat_choose_member_empty)
    public void invitationDialogExecution() {
        presenter.invite();

    }

    public void onEvent(MemberStarredEvent event) {
        presenter.initMembers();
    }
}
