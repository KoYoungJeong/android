package com.tosslab.jandi.app.ui.entities.chats;

import android.app.Fragment;
import android.text.TextUtils;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
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

    @Bean
    ChatChooseModel chatChooseModel;

    ChatChooseAdapter chatChooseAdapter;
    private PublishSubject<String> publishSubject;

    @AfterViews
    void initViews() {
        List<ChatChooseItem> chatListWithoutMe = chatChooseModel.getChatListWithoutMe();

        chatChooseAdapter = new ChatChooseAdapter(getActivity());
        chatListView.setAdapter(chatChooseAdapter);

        chatChooseAdapter.clear();
        chatChooseAdapter.addAll(chatListWithoutMe);
        chatChooseAdapter.notifyDataSetChanged();

        initSearchTextObserver();
    }

    private void initSearchTextObserver() {
        publishSubject = PublishSubject.create();
        publishSubject
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(name -> {
                    if (!TextUtils.isEmpty(name)) {
                        return chatChooseModel.getChatListWithoutMe(name);
                    } else {
                        return chatChooseModel.getChatListWithoutMe();
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

    @ItemClick(R.id.list_chat_choose)
    void onEntitySelect(ChatChooseItem chatChooseItem) {

        getActivity().finish();

        int entityId = chatChooseItem.getEntityId();
        MessageListV2Activity_.intent(getActivity())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .teamId(chatChooseModel.getTeamId())
                .isFavorite(chatChooseItem.isStarred())
                .start();
    }

    @TextChange(R.id.et_chat_choose_search)
    void onSearchTextChange(CharSequence text) {
        publishSubject.onNext(text.toString());
    }

}
