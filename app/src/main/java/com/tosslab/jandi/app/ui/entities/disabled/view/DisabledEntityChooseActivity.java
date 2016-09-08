package com.tosslab.jandi.app.ui.entities.disabled.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenter;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenterImpl;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_disabled_entity_choose)
public class DisabledEntityChooseActivity extends BaseAppCompatActivity implements DisabledEntityChoosePresenter.View {

    public static final String EXTRA_RESULT = "result";
    @Bean(DisabledEntityChoosePresenterImpl.class)
    DisabledEntityChoosePresenter presenter;
    @ViewById(R.id.lv_disabled_choose)
    RecyclerView lvMembers;

    ChatChooseAdapter adapter;

    @AfterInject
    void initObject() {
        adapter = new ChatChooseAdapter(DisabledEntityChooseActivity.this);
        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            onMemberItemClick(position);
        });
        presenter.setView(this);
    }

    @AfterViews
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
        MemberProfileActivity_.intent(DisabledEntityChooseActivity.this)
                .memberId(event.userId)
                .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();

        AnalyticsValue.Action action = AnalyticsUtil.getProfileAction(event.userId, event.from);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamMembers, action);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
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
