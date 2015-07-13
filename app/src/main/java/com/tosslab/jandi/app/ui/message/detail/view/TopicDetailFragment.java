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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteTopicDialogFragment;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.edit.TopicDescriptionEditActivity;
import com.tosslab.jandi.app.ui.message.detail.edit.TopicDescriptionEditActivity_;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenter;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenterImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
@EFragment(R.layout.fragment_topic_detail)
@OptionsMenu(R.menu.topic_detail)
public class TopicDetailFragment extends Fragment implements TopicDetailPresenter.View {

    @FragmentArg
    int entityId;


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
    @ViewById(R.id.iv_topic_detail_starred)
    View ivStarred;
    private boolean owner;
    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        topicDetailPresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        setUpActionbar();

        topicDetailPresenter.onInit(getActivity(), entityId);
    }

    private void setUpActionbar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_topic_detail);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

            actionBar.setTitle(R.string.jandi_topic_detail);
        }
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
        topicDetailPresenter.onConfirmChangeTopicName(getActivity()
                , event.topicId
                , event.inputName
                , event.topicType);
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        topicDetailPresenter.onInit(getActivity(), entityId);
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
        topicDetailPresenter.onChangeTopicName(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_description)
    void onTopicDescriptionClick() {
        topicDetailPresenter.onTopicDescriptionMove(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_member_count)
    void onTopicParticipantsClick() {
        MembersListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .type(JandiConstants.TYPE_MEMBERS_LIST_TOPIC)
                .entityId(entityId)
                .start();
    }

    @OptionsItem(R.id.action_add_member)
    @Click(R.id.vg_topic_detail_invite)
    void onTopicInviteClick() {
        topicDetailPresenter.onTopicInvite(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_starred)
    void onTopicStarClick() {
        topicDetailPresenter.onTopicStar(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_leave)
    void onTopicLeaveClick() {
        topicDetailPresenter.onTopicLeave(getActivity(), entityId);
    }

    @Click(R.id.vg_topic_detail_delete)
    void onTopicDeleteClick() {
        topicDetailPresenter.onTopicDelete(getActivity(), entityId);
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
    public void showSuccessToast(String message) {
        ColoredToast.show(getActivity(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailToast(String message) {
        ColoredToast.showWarning(getActivity(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEnableTopicDelete(boolean owner) {
        if (owner) {
            vgDelete.setVisibility(View.VISIBLE);
        } else {
            vgDelete.setVisibility(View.GONE);
        }
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showTopicNameChangeDialog(int entityId, String entityName, int entityType) {
        android.app.DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_TOPIC
                , entityType
                , entityId
                , entityName);
        newFragment.show(getActivity().getFragmentManager(), "dialog");
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
