package com.tosslab.jandi.app.ui.message.detail.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteTopicDialogFragment;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.edit.TopicDescriptionEditActivity;
import com.tosslab.jandi.app.ui.message.detail.edit.TopicDescriptionEditActivity_;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenter;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenterImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
@EFragment(R.layout.fragment_topic_detail)
public class TopicDetailFragment extends Fragment implements TopicDetailPresenter.View {

    @FragmentArg
    int entityId;
    @FragmentArg
    int teamId;

    @Bean(TopicDetailPresenterImpl.class)
    TopicDetailPresenter topicDetailPresenter;

    @ViewById(R.id.tv_topic_detail_title)
    TextView tvTitle;
    @ViewById(R.id.tv_topic_detail_description)
    TextView tvDescription;

    @ViewById(R.id.vg_topic_detail_member_count)
    View vgMemberCount;
    @ViewById(R.id.tv_topic_detail_member_count)
    TextView tvMemberCount;

    @ViewById(R.id.vg_topic_detail_invite)
    View vgInvite;

    @ViewById(R.id.vg_topic_detail_delete)
    View vgDelete;
    @ViewById(R.id.vg_topic_detail_leave)
    View vgLeave;
    @ViewById(R.id.view_topic_detail_leve_to_delete)
    View viewDividerDelete;
    @ViewById(R.id.vg_topic_detail_default_message)
    View vgDefaultMessage;

    @ViewById(R.id.iv_topic_detail_starred)
    View ivStarred;
    @ViewById(R.id.switch_topic_detail_set_push)
    SwitchCompat switchSetPush;
    @ViewById(R.id.switch_topic_detail_set_auto_join)
    SwitchCompat switchAutoJoin;
    @ViewById(R.id.vg_topic_detail_set_auto_join)
    ViewGroup vgAutoJoin;
    @ViewById(R.id.tv_topic_detail_set_push)
    TextView tvSetPush;


    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        topicDetailPresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        setUpActionbar();

        topicDetailPresenter.onInit(getActivity(), entityId);

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.TopicDescription);
    }

    private void setUpActionbar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_topic_detail);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
        }

        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

            actionBar.setTitle(R.string.jandi_topic_detail);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.topic_detail, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(InvitationSuccessEvent event) {
        topicDetailPresenter.onInit(getActivity(), entityId);
    }

    public void onEvent(TopicLeaveEvent event) {
        leaveTopic();
    }

    public void onEvent(ConfirmModifyTopicEvent event) {
        topicDetailPresenter.onConfirmChangeTopicName(getActivity(),
                event.topicId,
                event.inputName,
                event.topicType);
    }

    public void onEventMainThread(TopicInfoUpdateEvent event) {
        topicDetailPresenter.onInit(getActivity(), entityId);
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {
        FormattedEntity entity =
                EntityManager.getInstance().getEntityById(entityId);
        if (entity == EntityManager.UNKNOWN_USER_ENTITY) {
            return;
        }
        topicDetailPresenter.onInit(getActivity(), entityId);
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (event.getId() == entityId) {
            leaveTopic();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void leaveTopic() {
        FragmentActivity activity = getActivity();
        Intent data = new Intent();
        data.putExtra(TopicDetailActivity.EXTRA_LEAVE, true);

        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    public void onEvent(ConfirmDeleteTopicEvent event) {
        topicDetailPresenter.deleteTopic(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_name)
    void onTopicNameClick() {
        topicDetailPresenter.onChangeTopicName(entityId);
    }

    @Click(R.id.vg_topic_detail_description)
    void onTopicDescriptionClick() {
        topicDetailPresenter.onTopicDescriptionMove(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TopicDescription);
    }

    @Click(R.id.vg_topic_detail_member_count)
    void onTopicParticipantsClick() {
        MembersListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .type(MembersListActivity.TYPE_MEMBERS_LIST_TOPIC)
                .entityId(entityId)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Participants);
    }

    @Click(R.id.vg_topic_detail_set_auto_join)
    void onAutoJoinClick() {
        topicDetailPresenter.onAutoJoin(entityId, !switchAutoJoin.isChecked());
    }

    // Topic Push
    @Click(R.id.vg_topic_detail_set_push)
    void onPushClick() {
        boolean checked = !switchSetPush.isChecked();

        setTopicPushSwitch(checked);

        topicDetailPresenter.updateTopicPushSubscribe(getActivity(), teamId, entityId, checked);

        if (checked) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOnNotifications);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOffNotifications);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTopicPushSwitch(boolean isPushOn) {
        switchSetPush.setChecked(isPushOn);
    }

    @OptionsItem(R.id.action_add_member)
    @Click(R.id.vg_topic_detail_invite)
    void onTopicInviteClick() {
        topicDetailPresenter.onTopicInvite(getActivity(), entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.InviteTeamMembers);
    }

    @Click(R.id.vg_topic_detail_starred)
    void onTopicStarClick() {
        topicDetailPresenter.onTopicStar(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_leave)
    void onTopicLeaveClick() {
        topicDetailPresenter.onTopicLeave(getActivity(), entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Leave);
    }

    @Click(R.id.vg_topic_detail_delete)
    void onTopicDeleteClick() {
        topicDetailPresenter.onTopicDelete(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Delete);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTopicName(String topicName) {
        tvTitle.setText(topicName);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTopicDescription(String topicDescription) {
        tvDescription.setText(topicDescription);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setStarred(boolean isStarred) {
        ivStarred.setSelected(isStarred);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTopicMemberCount(int topicMemberCount) {
        tvMemberCount.setText(String.valueOf(topicMemberCount));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setLeaveVisible(boolean owner, boolean defaultTopic) {
        if (defaultTopic) {
            vgLeave.setVisibility(View.GONE);
            vgDelete.setVisibility(View.GONE);
            vgDefaultMessage.setVisibility(View.VISIBLE);
            viewDividerDelete.setVisibility(View.GONE);

        } else {
            vgLeave.setVisibility(View.VISIBLE);
            if (owner) {
                viewDividerDelete.setVisibility(View.VISIBLE);
                vgDelete.setVisibility(View.VISIBLE);
            } else {
                viewDividerDelete.setVisibility(View.GONE);
                vgDelete.setVisibility(View.GONE);
            }
            vgDefaultMessage.setVisibility(View.GONE);


        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setTopicAutoJoin(boolean autoJoin, boolean owner, boolean defaultTopic, boolean privateTopic) {
        if (privateTopic) {
            if (owner) {
                vgAutoJoin.setEnabled(true);
            } else {
                vgAutoJoin.setEnabled(false);
            }
            switchAutoJoin.setChecked(false);
        } else if (defaultTopic) {
            switchAutoJoin.setChecked(true);
            vgAutoJoin.setEnabled(true);
        } else if (owner) {
            switchAutoJoin.setChecked(autoJoin);
            vgAutoJoin.setEnabled(true);
        } else {
            switchAutoJoin.setChecked(autoJoin);
            vgAutoJoin.setEnabled(false);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(getActivity(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailToast(String message) {
        ColoredToast.showWarning(getActivity(), message);
    }

    @Override
    public void showTopicDeleteDialog() {
        DialogFragment newFragment = DeleteTopicDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }

        if (progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        progressWheel.show();
    }

    @UiThread(delay = 200L)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showTopicNameChangeDialog(int entityId, String entityName, int entityType) {
        android.app.DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_TOPIC, entityType, entityId, entityName);
        newFragment.show(getActivity().getFragmentManager(), "dialog");

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TopicName);
    }

    @OnActivityResult(TopicDescriptionEditActivity.REQUEST_EDIT)
    void onDescriptionEditResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        topicDetailPresenter.onInit(getActivity(), entityId);
    }

    @Override
    public void moveTopicDescriptionEdit() {
        TopicDescriptionEditActivity_.intent(TopicDetailFragment.this)
                .entityId(entityId)
                .startForResult(TopicDescriptionEditActivity.REQUEST_EDIT);
    }

}
