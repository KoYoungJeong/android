package com.tosslab.jandi.app.ui.entities.disabled.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.f2prateek.dart.HensonNavigable;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.disabled.dagger.DaggerDisabledEntityChooseComponent;
import com.tosslab.jandi.app.ui.entities.disabled.dagger.DisabledEntityChooseModule;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenter;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

@HensonNavigable
public class DisabledEntityChooseActivity extends BaseAppCompatActivity implements DisabledEntityChoosePresenter.View {

    public static final String EXTRA_RESULT = "result";
    @Inject
    DisabledEntityChoosePresenter presenter;
    @Bind(R.id.lv_disabled_choose)
    RecyclerView lvMembers;

    ChatChooseAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_entity_choose);
        ButterKnife.bind(this);
        DaggerDisabledEntityChooseComponent.builder()
                .disabledEntityChooseModule(new DisabledEntityChooseModule(this))
                .build()
                .inject(this);
        initObject();
        initViews();
    }

    void initObject() {
        adapter = new ChatChooseAdapter(DisabledEntityChooseActivity.this);
        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            onMemberItemClick(position);
        });
    }

    void initViews() {
        initActionBarTitle();
        presenter.initDisabledMembers();
        lvMembers.setLayoutManager(new LinearLayoutManager(DisabledEntityChooseActivity.this));
        lvMembers.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void setDisabledMembers(List<ChatChooseItem> disabledMembers) {
        adapter.clear();
        adapter.addAll(disabledMembers);
        adapter.notifyDataSetChanged();
    }

    void initActionBarTitle() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_disabled_choose);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }
    }

    public void onEventMainThread(ShowProfileEvent event) {
        if (AccessLevelUtil.hasAccessLevel(event.userId)) {

            startActivity(Henson.with(this)
                    .gotoMemberProfileActivity()
                    .memberId(event.userId)
                    .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(this);
        }

        AnalyticsValue.Action action = AnalyticsUtil.getProfileAction(event.userId, event.from);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamMembers, action);
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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.ready, R.anim.slide_out_to_bottom);
    }

    void onMemberItemClick(int position) {
        ChatChooseItem item = adapter.getItem(position);
        long entityId = item.getEntityId();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, entityId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
