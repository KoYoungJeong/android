package com.tosslab.jandi.app.ui.share.views;

/**
 * Created by tee on 15. 9. 15..
 */

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareRoomsAdapter;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.layout_room_selector)
public class ShareSelectRoomActivity extends BaseAppCompatActivity implements ShareRoomsAdapter.OnItemClickListener {

    @Extra
    int teamId;

    @Bean
    ShareSelectModel shareSelectModel;

    @ViewById(R.id.tv_room_selector_topic)
    View topicView;
    @ViewById(R.id.tv_room_selector_direct_message)
    View dmView;
    @ViewById(R.id.rv_room_selector)
    RecyclerView lvRoomSelect;

    ShareRoomsAdapter adapter;


    @AfterInject
    void initObject() {

        adapter = new ShareRoomsAdapter(this);
        adapter.setOnItemClickListener(this);
    }

    @Background
    void initFormattedEntities() {
        shareSelectModel.initFormattedEntities(teamId);
        getTopics();
    }

    @AfterViews
    void initViews() {
        setupActionbar();
        lvRoomSelect.setLayoutManager(new LinearLayoutManager(this));
        lvRoomSelect.setAdapter(adapter);
        initFormattedEntities();

        View[] selectableViews = {topicView, dmView};

        topicView.setOnClickListener(v -> {
            setSelectType(0, selectableViews);
            getTopics();
            lvRoomSelect.getLayoutManager().scrollToPosition(0);
        });

        dmView.setOnClickListener(v -> {
            setSelectType(1, selectableViews);
            getDms();
            lvRoomSelect.getLayoutManager().scrollToPosition(0);
        });
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

    @Background
    void getTopics() {
        List<ExpandRoomData> topicDatas =
                shareSelectModel.getTopicDatas(teamId);
        showTopics(topicDatas);
    }

    @Background
    void getDms() {
        List<ExpandRoomData> userRoomDatas =
                shareSelectModel.getUserRoomDatas();
        showDms(userRoomDatas);
    }


    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showTopics(List<ExpandRoomData> topics) {
        adapter.addAll(topics);
        adapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showDms(List<ExpandRoomData> dms) {
        adapter.addAll(dms);
        adapter.notifyDataSetChanged();
    }


    private void setSelectType(int type, View[] selectableViews) {
        for (int idx = 0, size = selectableViews.length; idx < size; idx++) {
            if (idx == type) {
                selectableViews[idx].setSelected(true);
            } else {
                selectableViews[idx].setSelected(false);
            }
        }
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_share_to_jandi);

    }

    @Override
    public void onItemClick(int roomId, String roomName, int roomType) {
        ShareSelectRoomEvent event = new ShareSelectRoomEvent();
        event.setRoomId(roomId);
        event.setRoomName(roomName);
        event.setRoomType(roomType);
        EventBus.getDefault().post(event);
        finish();
    }

}
