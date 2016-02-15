package com.tosslab.jandi.app.ui.entities.disabled.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.view.ChatsChooseFragment;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenter;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenterImpl;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_disabled_entity_choose)
public class DisabledEntityChooseActivity extends BaseAppCompatActivity implements DisabledEntityChoosePresenter.View {

    @Bean(DisabledEntityChoosePresenterImpl.class)
    DisabledEntityChoosePresenter presenter;
    @ViewById(R.id.lv_disabled_choose)
    ListView lvMembers;

    ChatChooseAdapter adapter;

    @AfterInject
    void initObject() {
        adapter = new ChatChooseAdapter(DisabledEntityChooseActivity.this);
        presenter.setView(this);
    }

    @AfterViews
    void initViews() {
        initActionBarTitle();
        presenter.initDisabledMembers();
        lvMembers.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
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

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.ready, R.anim.slide_out_to_bottom);
    }

    @ItemClick(R.id.lv_disabled_choose)
    void onMemberItemClick(int position) {
        ChatChooseItem item = adapter.getItem(position);
        long entityId = item.getEntityId();
        Intent intent = new Intent();
        intent.putExtra(ChatsChooseFragment.EXTRA_ENTITY_ID, entityId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
