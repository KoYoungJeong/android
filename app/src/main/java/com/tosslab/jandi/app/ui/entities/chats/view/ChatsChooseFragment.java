package com.tosslab.jandi.app.ui.entities.chats.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataView;
import com.tosslab.jandi.app.ui.entities.chats.dagger.ChatChooseModule;
import com.tosslab.jandi.app.ui.entities.chats.dagger.DaggerChatChooseComponent;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenter;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;

public class ChatsChooseFragment extends Fragment implements ChatChoosePresenter.View {

    public static final String EXTRA_ENTITY_ID = "entity_id";
    private static final int REQ_DISABLED_MEMBERS = 901;
    @Bind(R.id.lv_chat_choose)
    RecyclerView lvChatChoose;

    @Bind(R.id.layout_member_empty)
    View emptyMemberView;

    @Bind(R.id.et_chat_choose_search)
    TextView tvSearch;
    @Inject
    ChatChooseAdapterDataView chatChooseAdapterDataView;
    @Inject
    InputMethodManager inputMethodManager;

    @Inject
    ChatChoosePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_choose, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChatChooseAdapter chatChooseAdapter = new ChatChooseAdapter(getActivity());
        chatChooseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (chatChooseAdapter.isEmpty()) {
                    emptyMemberView.setVisibility(View.VISIBLE);
                } else {
                    emptyMemberView.setVisibility(View.GONE);
                }
            }
        });

        chatChooseAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            presenter.onItemClick(position);
        });

        lvChatChoose.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvChatChoose.addItemDecoration(new SimpleDividerItemDecoration());
        lvChatChoose.setAdapter(chatChooseAdapter);

        DaggerChatChooseComponent.builder()
                .chatChooseModule(new ChatChooseModule(this, chatChooseAdapter))
                .build()
                .inject(this);

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

    @Override
    public void moveChatMessage(long teamId, long entityId) {
        getActivity().finish();
        startActivity(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(-1)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void refresh() {
        chatChooseAdapterDataView.refresh();
    }

    @Override
    public void MoveDisabledEntityList() {
        startActivityForResult(Henson.with(getActivity())
                .gotoDisabledEntityChooseActivity()
                .build(), REQ_DISABLED_MEMBERS);
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
    }


    public void onEvent(TeamJoinEvent event) {
        presenter.onSearch(tvSearch.getText().toString());
    }

    public void onEvent(TeamLeaveEvent event) {
        presenter.onSearch(tvSearch.getText().toString());
    }

    public void onEventMainThread(ShowProfileEvent event) {
        if (AccessLevelUtil.hasAccessLevel(event.userId)) {
            startActivity(Henson.with(getActivity())
                    .gotoMemberProfileActivity()
                    .memberId(event.userId)
                    .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(getActivity());
        }

        AnalyticsValue.Action action = AnalyticsUtil.getProfileAction(event.userId, event.from);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamMembers, action);
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        presenter.onMoveChatMessage(event.userId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_DISABLED_MEMBERS) {
            onDisabledMemberActivityResult(resultCode, data);
        }
    }

    void onDisabledMemberActivityResult(int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (data.hasExtra(EXTRA_ENTITY_ID)) {
            presenter.onMoveChatMessage(data.getLongExtra(EXTRA_ENTITY_ID, -1));
        }
    }

    @OnEditorAction(R.id.et_chat_choose_search)
    boolean onSearchTextImeAction(TextView textView) {
        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        return true;
    }

    @OnTextChanged(R.id.et_chat_choose_search)
    void onSearchTextChange(CharSequence text) {
        presenter.onSearch(text.toString());
    }

    @OnClick(R.id.btn_chat_choose_member_empty)
    public void invitationDialogExecution() {
        presenter.invite(getContext());

    }

    public void onEvent(MemberStarredEvent event) {
        presenter.initMembers();
    }
}
