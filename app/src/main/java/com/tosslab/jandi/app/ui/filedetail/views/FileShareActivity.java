package com.tosslab.jandi.app.ui.filedetail.views;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.utils.ColoredToast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FileShareActivity extends BaseAppCompatActivity {
    public static final String KEY_ENTITY_ID = "entity_id";

    @InjectExtra
    long fileId;
    @Bind(R.id.layout_search_bar)
    Toolbar toolbar;
    @Bind(R.id.view_container)
    View container;

    @Inject
    FileDetailModel fileDetailModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_share_entity_choose);

        ButterKnife.bind(this);
        Dart.inject(this);
        DaggerFileShareComponent.builder()
                .build()
                .inject(this);

        initViews();
    }

    void initViews() {
        setupActionbar();
        showList();
    }

    void setupActionbar() {
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_title_cdp_to_be_shared);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showList() {

        List<TopicRoom> topics = fileDetailModel.getTopicRooms();
        List<Member> users = fileDetailModel.getMembers();

        if (!topics.isEmpty() || !users.isEmpty()) {
            RoomSelector roomSelector = new RoomSelectorImpl(topics, users);
            roomSelector.setOnRoomSelectListener(item -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_ENTITY_ID, item.getEntityId());
                setResult(RESULT_OK, returnIntent);
                finish();
            });
            roomSelector.setType(RoomSelectorImpl.TYPE_VIEW);
            roomSelector.show(container);
        } else {
            showErrorToast(getString(R.string.err_file_already_shared_all_topics));
            finish();
        }
    }

    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

}