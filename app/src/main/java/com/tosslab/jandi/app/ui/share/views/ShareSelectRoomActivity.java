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
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareRoomsAdapter;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.LinkedHashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

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

    ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        adapter = new ShareRoomsAdapter(this);
        adapter.setOnItemClickListener(this);
    }

    @AfterViews
    void initViews() {
        setupActionbar();
        lvRoomSelect.setLayoutManager(new LinearLayoutManager(this));
        lvRoomSelect.setAdapter(adapter);

        View[] selectableViews = {topicView, dmView};

        topicView.setOnClickListener(v -> {
            setSelectType(0, selectableViews);
            getTopics();
            lvRoomSelect.getLayoutManager().scrollToPosition(0);
        });

        dmView.setOnClickListener(v -> {
            setSelectType(1, selectableViews);
            getDirectMessages();
            lvRoomSelect.getLayoutManager().scrollToPosition(0);
        });

        selectableViews[0].setSelected(true);

        progressWheel = new ProgressWheel(this);
        progressWheel.setCancelable(false);

        initFormattedEntities();
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

    @Background(serial = "share_background")
    void initFormattedEntities() {
        showProgress();

        try {
            ResLeftSideMenu leftSideMenu;
            leftSideMenu = LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId);
            if (leftSideMenu == null) {
                leftSideMenu = shareSelectModel.getLeftSideMenu(teamId);
                LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
            }
            shareSelectModel.initFormattedEntities(leftSideMenu);
            getTopics();
        } catch (RetrofitError e) {
            e.printStackTrace();
            String errorMessage = getApplicationContext().getResources().getString(R.string.err_network);
            showError(errorMessage);
            hideProgress();
            finish();
        }
    }

    @Background(serial = "share_background")
    void getTopics() {
        List<ResFolder> topicFolders = shareSelectModel.getTopicFolders(teamId);
        List<ResFolderItem> topicFolderItems = shareSelectModel.getTopicFolderItems(teamId);
        LinkedHashMap<Integer, FormattedEntity> joinEntities = shareSelectModel.getJoinEntities();

        List<ExpandRoomData> topicDatas =
                shareSelectModel.getExpandRoomDatas(topicFolders, topicFolderItems, joinEntities);
        showTopics(topicDatas);

        hideProgress();
    }

    @Background
    void getDirectMessages() {
        showProgress();

        List<ExpandRoomData> userRoomDatas = shareSelectModel.getUserRoomDatas();
        showDirectMessages(userRoomDatas);

        hideProgress();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showTopics(List<ExpandRoomData> topics) {
        adapter.addAll(topics);
        adapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showDirectMessages(List<ExpandRoomData> dms) {
        adapter.addAll(dms);
        adapter.notifyDataSetChanged();
    }

    @UiThread
    void showProgress() {
        if (progressWheel == null || progressWheel.isShowing()) {
            return;
        }
        progressWheel.show();
    }

    @UiThread
    void hideProgress() {
        if (progressWheel == null || !progressWheel.isShowing()) {
            return;
        }
        progressWheel.hide();
    }

    @UiThread
    void showError(String message) {
        ColoredToast.showError(getApplicationContext(), message);
    }

    @UiThread
    @Override
    public void finish() {
        super.finish();
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
