package com.tosslab.jandi.app.ui.poll.participants;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.poll.participants.adapter.PollParticipantsAdapter;
import com.tosslab.jandi.app.ui.poll.participants.component.DaggerPollParticipantsComponent;
import com.tosslab.jandi.app.ui.poll.participants.module.PollParticipantsModule;
import com.tosslab.jandi.app.ui.poll.participants.presenter.PollParticipantsPresenter;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollParticipantsActivity extends AppCompatActivity
        implements PollParticipantsPresenter.View {

    public static final String KEY_POLL_ID = "pollId";
    public static final String KEY_SEQUENCE = "sequence";
    public static final String KEY_TITLE = "title";

    @Bind(R.id.lv_poll_participants)
    RecyclerView lvParticipants;
    @Bind(R.id.toolbar_poll_participants)
    Toolbar toolbar;

    @Inject
    PollParticipantsPresenter presenter;

    private PollParticipantsAdapter pollParticipantsAdapter;
    private ProgressWheel progressWheel;

    public static void startForAllParticipants(Activity activity, long pollId) {
        Intent intent = new Intent(activity, PollParticipantsActivity.class);
        intent.putExtra(KEY_POLL_ID, pollId);
        activity.startActivity(intent);
    }

    public static void start(Activity activity, long pollId, Poll.Item item) {
        Intent intent = new Intent(activity, PollParticipantsActivity.class);
        intent.putExtra(KEY_POLL_ID, pollId);
        intent.putExtra(KEY_SEQUENCE, item.getSeq());
        intent.putExtra(KEY_TITLE, item.getName());
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_participants);

        injectComponent();

        ButterKnife.bind(this);

        setupActionBar();

        initProgressWheel();

        initPollParticipantsListView();

        initPollParticipants(getIntent());
    }

    private void initPollParticipants(Intent intent) {
        long pollId = intent.getLongExtra(KEY_POLL_ID, -1);
        if (pollId == -1) {
            showUnExpectedErrorToast();
            finish();
            return;
        }

        int seq = intent.getIntExtra(KEY_SEQUENCE, -1);
        String title = intent.getStringExtra(KEY_TITLE);
        presenter.onInitializePollParticipants(pollId, seq, title);
    }

    private void injectComponent() {
        DaggerPollParticipantsComponent.builder()
                .pollParticipantsModule(new PollParticipantsModule(this))
                .build()
                .inject(this);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle("참여자");
    }

    private void initPollParticipantsListView() {
        pollParticipantsAdapter = new PollParticipantsAdapter();
        lvParticipants.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        lvParticipants.setAdapter(pollParticipantsAdapter);

        pollParticipantsAdapter.setOnMemberClickListener(member -> {
            MemberProfileActivity_.intent(PollParticipantsActivity.this)
                    .memberId(member.getId())
                    .start();
        });
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showCheckNetworkDialog(boolean shouldFinishWhenConfirm) {
        runOnUiThread(() -> {
            DialogInterface.OnClickListener confirmListener = null;
            if (shouldFinishWhenConfirm) {
                confirmListener = (dialog, which) -> finish();
            }

            AlertUtil.showCheckNetworkDialog(this, confirmListener);
        });
    }

    @Override
    public void showUnExpectedErrorToast() {
        ColoredToast.showError(R.string.jandi_err_unexpected);
    }

    @Override
    public void addMembers(List<User> members) {
        Observable.from(members)
                .map(member -> MultiItemRecyclerAdapter.Row.create(
                        member, PollParticipantsAdapter.VIEW_TYPE_MEMBER))
                .subscribe(row -> pollParticipantsAdapter.addRow(row),
                        Throwable::printStackTrace,
                        () -> pollParticipantsAdapter.notifyDataSetChanged());
    }

    @Override
    public void setTitle(String headerTitle) {
        if (!TextUtils.isEmpty(headerTitle)) {
            MultiItemRecyclerAdapter.Row row =
                    MultiItemRecyclerAdapter.Row.create(
                            headerTitle, PollParticipantsAdapter.VIEW_TYPE_TITLE);
            pollParticipantsAdapter.addRow(0, row);
            pollParticipantsAdapter.notifyDataSetChanged();
        }
    }
}
