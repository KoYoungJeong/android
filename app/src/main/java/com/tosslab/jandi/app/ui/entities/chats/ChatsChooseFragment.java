package com.tosslab.jandi.app.ui.entities.chats;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.to.DisableDummyItem;
import com.tosslab.jandi.app.ui.invites.InviteActivity_;
import com.tosslab.jandi.app.ui.invites.InviteUtils;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
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
    private PublishSubject<String> publishSubject;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    private String invitationUrl;
    private String teamName;

    private EntityManager mEntityManager;
    private ProgressWheel progressWheel;
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

        chatChooseAdapter.clear();

        List<ChatChooseItem> users = chatChooseModel.getUsers();
        chatChooseAdapter.addAll(users);

        chatChooseAdapter.notifyDataSetChanged();

        initSearchTextObserver();

        mEntityManager = EntityManager.getInstance(getActivity());
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
        EntityManager entityManager = EntityManager.getInstance(getActivity());
        MessageListV2Activity_.intent(getActivity())
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .roomId(-1)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
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

    @TextChange(R.id.et_chat_choose_search)
    void onSearchTextChange(CharSequence text) {
        publishSubject.onNext(text.toString());
    }

    @Click(R.id.layout_member_empty)
    @Background
    public void onInvitationDisableCheck() {
        showProgressWheel();

        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, mEntityManager.getTeamId());

        dismissProgressWheel();

        switch (result.first) {
            case NETWORK_ERROR:
                showErrorToast(getResources().getString(R.string.err_network));
                break;
            case ERROR:
                break;
            case INVITATION_DISABLED:
                showTextDialog(
                        getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                break;
            case UNDEFINED_URL:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case SUCCESS:
                moveToInvitationActivity(result.second);
                break;
            default:
                break;
        }
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(getActivity()).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    @UiThread
    public void moveToInvitationActivity(ResTeamDetailInfo.InviteTeam inviteTeam) {
        invitationUrl = inviteTeam.getInvitationUrl();
        teamName = inviteTeam.getName();
        DialogFragment invitationDialog = new InvitationDialogFragment();
        invitationDialog.show(getFragmentManager(), "invitationsDialog");
    }

    public void onEvent(TeamInvitationsEvent event) {
        String invitationContents =
                teamName + getResources().getString(R.string.jandi_invite_contents);
        int eventType = event.type;
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink(invitationUrl, invitationContents);
            showTextDialog(getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = InviteUtils.getInviteIntent(
                    getActivity(), event, invitationUrl, invitationContents);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink(invitationUrl, invitationContents);
                showTextDialog(getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    public void copyLink(String publicLink, String contents) {
        ClipData clipData = ClipData.newPlainText("", contents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(getActivity(), message);
    }

    @UiThread
    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
            progressWheel.init();
        }

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(getActivity())
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }
}
