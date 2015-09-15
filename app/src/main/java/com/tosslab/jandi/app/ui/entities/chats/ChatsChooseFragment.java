package com.tosslab.jandi.app.ui.entities.chats;

import android.content.ClipboardManager;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.to.DisableDummyItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
@EFragment(R.layout.fragment_chat_choose)
public class ChatsChooseFragment extends Fragment {

    @ViewById(R.id.list_chat_choose)
    ListView chatListView;

    @ViewById(R.id.layout_member_empty)
    View emptyMemberView;

    @Bean
    ChatChooseModel chatChooseModel;

    ChatChooseAdapter chatChooseAdapter;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @SystemService
    ClipboardManager clipboardManager;
    @SystemService
    InputMethodManager inputMethodManager;
    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    private PublishSubject<String> publishSubject;

    @AfterViews
    void initViews() {
        chatChooseAdapter = new ChatChooseAdapter(getActivity());
        chatChooseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                List<ChatChooseItem> tempUsers = chatChooseModel.getUsers();

                if (tempUsers == null || tempUsers.isEmpty()) {
                    emptyMemberView.setVisibility(View.VISIBLE);
                } else {
                    emptyMemberView.setVisibility(View.GONE);
                }
            }
        });

        chatListView.setAdapter(chatChooseAdapter);

        setUpMember();

        initSearchTextObserver();

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setUpMember() {
        chatChooseAdapter.clear();
        List<ChatChooseItem> users = chatChooseModel.getUsers();
        chatChooseAdapter.addAll(users);
        chatChooseAdapter.notifyDataSetChanged();
    }

    private void initSearchTextObserver() {
        publishSubject = PublishSubject.create();
        publishSubject
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(name -> {
                    if (!TextUtils.isEmpty(name)) {
                        return chatChooseModel.getChatListWithoutMe(name);
                    } else {
                        return chatChooseModel.getUsers();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatChooseItems -> {
                    chatChooseAdapter.clear();
                    chatChooseAdapter.addAll(chatChooseItems);
                    chatChooseAdapter.notifyDataSetChanged();
                });
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

    public void onEventMainThread(ProfileDetailEvent event) {
        UserInfoDialogFragment_.builder().entityId(event.getEntityId()).build().show(getFragmentManager(), "dialog");

    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        getActivity().finish();
        EntityManager entityManager = EntityManager.getInstance();
        MessageListV2Activity_.intent(getActivity())
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .roomId(-1)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .startForResult(MainTabActivity.REQ_START_MESSAGE);
    }

    @ItemClick(R.id.list_chat_choose)
    void onEntitySelect(int position) {
        ChatChooseItem chatChooseItem = chatChooseAdapter.getItem(position);
        if (chatChooseItem instanceof DisableDummyItem) {
            chatChooseAdapter.remove(chatChooseItem);
            chatChooseAdapter.notifyDataSetChanged();

            chatListView.smoothScrollToPositionFromTop(position, chatListView.getChildAt(0).getHeight() / 2);

        } else {

            getActivity().finish();
            int entityId = chatChooseItem.getEntityId();
            MessageListV2Activity_.intent(getActivity())
                    .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                    .entityId(entityId)
                    .teamId(chatChooseModel.getTeamId())
                    .roomId(-1)
                    .isFavorite(chatChooseItem.isStarred())
                    .start();
        }
    }

    @EditorAction(R.id.et_chat_choose_search)
    void onSearchTextImeAction(TextView textView) {
        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
    }

    @TextChange(R.id.et_chat_choose_search)
    void onSearchTextChange(CharSequence text) {
        publishSubject.onNext(text.toString());
    }

    @Click(R.id.layout_member_empty)
    public void invitationDialogExecution() {
        invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_CHAT_CHOOSE);
        invitationDialogExecutor.execute();
    }

    public void onEvent(MemberStarredEvent event) {
        setUpMember();
    }

}
