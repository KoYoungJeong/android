package com.tosslab.jandi.app.ui.message.detail.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
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
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.dagger.DaggerTopicDetailComponent;
import com.tosslab.jandi.app.ui.message.detail.dagger.TopicDetailModule;
import com.tosslab.jandi.app.ui.message.detail.edit.TopicDescriptionEditActivity;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenter;
import com.tosslab.jandi.app.ui.settings.main.SettingsActivity;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class TopicDetailFragment extends Fragment implements TopicDetailPresenter.View {

    private static final String EXTRA_ENTITY_ID = "entityId";
    @Inject
    TopicDetailPresenter topicDetailPresenter;
    @Bind(R.id.tv_topic_detail_title)
    TextView tvTitle;
    @Bind(R.id.tv_topic_detail_description)
    TextView tvDescription;
    @Bind(R.id.vg_topic_detail_member_count)
    View vgMemberCount;
    @Bind(R.id.tv_topic_detail_member_count)
    TextView tvMemberCount;
    @Bind(R.id.vg_topic_detail_invite)
    View vgInvite;
    @Bind(R.id.vg_topic_detail_delete)
    View vgDelete;
    @Bind(R.id.vg_topic_detail_leave)
    View vgLeave;
    @Bind(R.id.vg_topic_detail_assign_topic_owner)
    View vgAssignTopicOwner;
    @Bind(R.id.view_topic_detail_leve_to_delete)
    View viewDividerDelete;
    @Bind(R.id.vg_topic_detail_default_message)
    View vgDefaultMessage;
    @Bind(R.id.vg_topic_detail_description)
    ViewGroup vgDescription;
    @Bind(R.id.iv_topic_detail_starred)
    View ivStarred;
    @Bind(R.id.switch_topic_detail_set_push)
    SwitchCompat switchSetPush;
    @Bind(R.id.switch_topic_detail_set_auto_join)
    SwitchCompat switchAutoJoin;
    @Bind(R.id.tv_topic_detail_set_auto_join)
    TextView tvAutoJoinStatus;
    @Bind(R.id.vg_topic_detail_set_auto_join_text)
    ViewGroup vgAutoJoinText;
    @Bind(R.id.tv_topic_detail_set_push)
    TextView tvSetPush;
    private long entityId;
    private long teamId;
    private ProgressWheel progressWheel;
    private AlertDialog globalPushDialog;

    public static Fragment createFragment(Context context, long entityId) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ENTITY_ID, entityId);
        return Fragment.instantiate(context, TopicDetailFragment.class.getName(), bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MembersListActivity.TYPE_ASSIGN_TOPIC_OWNER) {
            initViews();
        } else if (requestCode == TopicDescriptionEditActivity.REQUEST_EDIT) {
            onDescriptionEditResult(resultCode);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DaggerTopicDetailComponent.builder()
                .topicDetailModule(new TopicDetailModule(this))
                .build()
                .inject(this);

        initArgument();

        initViews();

    }

    private void initArgument() {
        Bundle bundle = getArguments();

        if (bundle != null) {
            entityId = bundle.getLong(EXTRA_ENTITY_ID, -1);
        }

        teamId = TeamInfoLoader.getInstance().getTeamId();

    }

    void initViews() {
        setUpActionbar();

        topicDetailPresenter.onInit(entityId);

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(InvitationSuccessEvent event) {
        topicDetailPresenter.onInit(entityId);
    }

    public void onEventMainThread(TopicLeaveEvent event) {
        leaveTopic();
    }

    public void onEvent(ConfirmModifyTopicEvent event) {
        topicDetailPresenter.onConfirmChangeTopicName(getActivity(),
                event.topicId,
                event.inputName,
                event.topicType);
    }

    public void onEventMainThread(TopicInfoUpdateEvent event) {
        topicDetailPresenter.onInit(entityId);
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {
        if (!TeamInfoLoader.getInstance().isTopic(entityId)) {
            return;
        }
        topicDetailPresenter.onInit(entityId);
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (event.getTopicId() == entityId) {
            leaveTopic();
        }
    }

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

    @OnClick(R.id.vg_topic_detail_name)
    void onTopicNameClick() {
        topicDetailPresenter.onChangeTopicName(entityId);
    }

    @OnClick(R.id.vg_topic_detail_description)
    void onTopicDescriptionClick() {
        topicDetailPresenter.onTopicDescriptionMove(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TopicDescription);
    }

    @OnClick(R.id.vg_topic_detail_member_count)
    void onTopicParticipantsClick() {
        startActivity(Henson.with(getActivity())
                .gotoMembersListActivity()
                .entityId(entityId)
                .type(MembersListActivity.TYPE_MEMBERS_LIST_TOPIC)
                .build().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Participants);
    }

    @OnClick(R.id.vg_topic_detail_set_auto_join)
    void onAutoJoinClick() {
        boolean futureAutoJoin = !switchAutoJoin.isChecked();
        topicDetailPresenter.onAutoJoin(entityId, futureAutoJoin);

        AnalyticsValue.Label label = futureAutoJoin ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off;
        AnalyticsUtil.sendEvent(
                AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.AutoInvitation, label);
    }

    // Topic Push
    @OnClick(R.id.vg_topic_detail_set_push)
    void onPushClick() {
        boolean checked = !switchSetPush.isChecked();

        setTopicPushSwitch(checked);

        topicDetailPresenter.onPushClick(teamId, entityId, checked);


        if (checked) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Notifications, AnalyticsValue.Label.On);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Notifications, AnalyticsValue.Label.Off);
        }
    }

    @Override
    public void setTopicPushSwitch(boolean isPushOn) {
        switchSetPush.setChecked(isPushOn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_member) {
            onTopicInviteClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vg_topic_detail_invite)
    void onTopicInviteClick() {

        topicDetailPresenter.onInviteMember(entityId);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.InviteTeamMembers);
    }

    @OnClick(R.id.vg_topic_detail_starred)
    void onTopicStarClick() {
        topicDetailPresenter.onTopicStar(entityId);
    }

    @OnClick(R.id.vg_topic_detail_assign_topic_owner)
    void onAssignTopicOwnerClick() {
        topicDetailPresenter.onAssignTopicOwner(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TransferTopicAdmin);
    }

    @OnClick(R.id.vg_topic_detail_leave)
    void onTopicLeaveClick() {
        topicDetailPresenter.onTopicLeave(getActivity(), entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Leave);
    }

    @OnClick(R.id.vg_topic_detail_delete)
    void onTopicDeleteClick() {
        topicDetailPresenter.onTopicDelete(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Delete);
    }

    @Override
    public void setTopicName(String topicName) {
        tvTitle.setText(topicName);
    }

    @Override
    public void setTopicDescription(String topicDescription) {
        tvDescription.setText(topicDescription);
    }

    @Override
    public void setStarred(boolean isStarred) {
        ivStarred.setSelected(isStarred);
    }

    @Override
    public void setTopicMemberCount(int topicMemberCount) {
        tvMemberCount.setText(String.valueOf(topicMemberCount));
    }

    @Override
    public void setLeaveVisible(boolean owner, boolean defaultTopic, boolean show) {
        if (show) {
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
        } else {
            vgLeave.setVisibility(View.GONE);
            vgDelete.setVisibility(View.GONE);
            vgDefaultMessage.setVisibility(View.GONE);
            viewDividerDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public void setTopicInviteEnabled(boolean enabled) {
        vgInvite.setEnabled(enabled);
    }

    @Override
    public void showDilaogInviteToDefaultTopic() {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.topic_default_invite_error_title)
                .setMessage(R.string.topic_default_invite_error_desc)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    @Override
    public void moveToInvite() {
        startActivity(Henson.with(getActivity())
                .gotoTeamMemberSearchActivity()
                .isSelectMode(true)
                .room_id(entityId)
                .from(TeamMemberSearchActivity.EXTRA_FROM_INVITE_TOPIC)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    public void showTopicDeleteAtLeastGuest() {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.topic_delete_associatejoined_title)
                .setMessage(R.string.topic_delete_associatejoined_desc)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_topic_delete, (dialog, which) -> {
                    topicDetailPresenter.deleteTopic(getActivity(), entityId);
                })
                .create()
                .show();
    }

    @Override
    public void showDialogNeedToAssignMember() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.topic_leave_associateintopic_title)
                .setMessage(R.string.topic_leave_associateintopic_desc)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    @Override
    public void setTopicAutoJoin(boolean autoJoin, boolean owner, boolean defaultTopic, boolean privateTopic, boolean enabled) {
        if (privateTopic) {
            vgAutoJoinText.setEnabled(false);
            switchAutoJoin.setChecked(false);
            switchAutoJoin.setVisibility(View.GONE);
            tvAutoJoinStatus.setVisibility(View.VISIBLE);
            tvAutoJoinStatus.setText(R.string.jandi_auto_join_off);
        } else if (defaultTopic) {
            vgAutoJoinText.setEnabled(false);
            switchAutoJoin.setChecked(true);
            switchAutoJoin.setVisibility(View.GONE);
            tvAutoJoinStatus.setVisibility(View.VISIBLE);
            tvAutoJoinStatus.setText(R.string.jandi_auto_join_on);
        } else if (owner) {
            switchAutoJoin.setChecked(autoJoin);
            vgAutoJoinText.setEnabled(true);
            switchAutoJoin.setVisibility(View.VISIBLE);
            tvAutoJoinStatus.setVisibility(View.GONE);
        } else {
            switchAutoJoin.setChecked(autoJoin);
            vgAutoJoinText.setEnabled(false);
            switchAutoJoin.setVisibility(View.GONE);
            tvAutoJoinStatus.setVisibility(View.VISIBLE);
        }
        tvAutoJoinStatus.setText(autoJoin ? R.string.jandi_auto_join_on : R.string.jandi_auto_join_off);

        vgAutoJoinText.setEnabled(enabled);
    }

    @Override
    public void showGlobalPushSetupDialog() {

        if (globalPushDialog == null) {
            globalPushDialog = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                    .setMessage(R.string.jandi_explain_global_push_off)
                    .setNegativeButton(R.string.jandi_close, (dialog1, which1) -> {
                        switchSetPush.setChecked(false);
                        topicDetailPresenter.onPushClick(teamId, entityId, false);
                    })
                    .setPositiveButton(R.string.jandi_go_to_setting, (dialog, which) -> {
                        movePushSettingActivity();
                    })
                    .setOnCancelListener(dialog2 -> {
                        switchSetPush.setChecked(false);
                        topicDetailPresenter.onPushClick(teamId, entityId, false);
                    })
                    .create();
            globalPushDialog.show();
        } else if (!globalPushDialog.isShowing()) {
            globalPushDialog.show();
        }
    }

    private void movePushSettingActivity() {
        Intent mainSettingIntent = new Intent(getActivity(), SettingsActivity.class);
        Intent pushSettingIntent = new Intent(getActivity(), SettingPushActivity.class);
        getActivity().startActivities(new Intent[]{mainSettingIntent, pushSettingIntent});
    }

    @Override
    public void setAssignTopicOwnerVisible(boolean owner) {
        vgAssignTopicOwner.setVisibility(owner ? View.VISIBLE : View.GONE);
        if (!owner) {
            ViewGroup parent = (ViewGroup) vgAssignTopicOwner.getParent();
            int assignTopicOwnerIdex = -1;
            for (int idx = 0, size = parent.getChildCount(); idx < size; idx++) {
                View child = parent.getChildAt(idx);

                if (child == vgAssignTopicOwner) {
                    assignTopicOwnerIdex = idx;
                    break;
                }
            }

            if (assignTopicOwnerIdex >= 0) {
                parent.getChildAt(assignTopicOwnerIdex + 1).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void moveToAssignTopicOwner() {

        startActivity(Henson.with(getActivity())
                .gotoMembersListActivity()
                .entityId(entityId)
                .type(MembersListActivity.TYPE_ASSIGN_TOPIC_OWNER)
                .build());
    }

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void showFailToast(String message) {
        ColoredToast.showWarning(message);
    }

    @Override
    public void showTopicDeleteDialogOnlyMember() {
        DialogFragment newFragment = DeleteTopicDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

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

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showTopicNameChangeDialog(long entityId, String entityName, int entityType) {
        android.app.DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_TOPIC, entityType, entityId, entityName);
        newFragment.show(getActivity().getFragmentManager(), "dialog");

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TopicName);
    }

    @Override
    public void showDialogNeedToAssignTopicOwner(String topicName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(topicName);
        builder.setMessage(R.string.jandi_need_to_assign_topic_owner);
        builder.setPositiveButton(R.string.jandi_confirm, null);
        builder.create().show();
    }

    void onDescriptionEditResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        topicDetailPresenter.onInit(entityId);
    }

    @Override
    public void moveTopicDescriptionEdit() {
        startActivity(Henson.with(getActivity())
                .gotoTopicDescriptionEditActivity()
                .entityId(entityId)
                .build());
    }


}
