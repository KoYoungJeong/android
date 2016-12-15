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
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareRoomsAdapter;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;
import rx.Observable;


@EActivity(R.layout.layout_room_selector)
public class ShareSelectRoomActivity extends BaseAppCompatActivity implements ShareRoomsAdapter.OnItemClickListener {

    @Extra
    long teamId;

    TeamInfoLoader teamInfoLoader;

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

            if (!InitialInfoRepository.getInstance().hasInitialInfo(teamId)) {
                InitialInfo initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);
                InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
                refreshRankIfNeed(teamId);
            }
            teamInfoLoader = TeamInfoLoader.getInstance(teamId);
            getTopics();
        } catch (RetrofitException e) {
            e.printStackTrace();
            String errorMessage = getApplicationContext().getResources().getString(R.string.err_network);
            showError(errorMessage);
            hideProgress();
            finish();
        }
    }

    private void refreshRankIfNeed(long teamId) {
        if (!RankRepository.getInstance().hasRanks(teamId)) {
            try {
                Ranks ranks = new TeamApi(RetrofitBuilder.getInstance()).getRanks(teamId);
                RankRepository.getInstance().addRanks(ranks.getRanks());
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
    }

    @Background(serial = "share_background")
    void getTopics() {
        List<TopicFolder> topicFolders = teamInfoLoader.getTopicFolders();

        List<ExpandRoomData> topicDatas = getExpandRoomDatas(topicFolders, teamInfoLoader);

        showTopics(topicDatas);

        hideProgress();
    }

    private List<ExpandRoomData> getExpandRoomDatas(List<TopicFolder> topicFolders,
                                                    TeamInfoLoader teamInfoLoader) {

        List<ExpandRoomData> expandRoomDatas = new ArrayList<>();
        Set<Long> addedRoomIds = new HashSet<>();
        Observable.from(topicFolders)
                .toSortedList((topicFolder, topicFolder2) -> topicFolder.getSeq() - topicFolder2.getSeq())
                .flatMap(Observable::from)
                .subscribe(topicFolder1 -> {
                    ExpandRoomData folderData = new ExpandRoomData();
                    folderData.setIsFolder(true);
                    folderData.setName(topicFolder1.getName());
                    expandRoomDatas.add(folderData);
                    List<TopicRoom> rooms = topicFolder1.getRooms();
                    Observable.from(rooms)
                            .filter(TopicRoom::isJoined)
                            .map(topicRoom -> {
                                ExpandRoomData roomData = new ExpandRoomData();
                                roomData.setName(topicRoom.getName());
                                roomData.setIsPublicTopic(topicRoom.isPublicTopic());
                                roomData.setIsStarred(topicRoom.isStarred());
                                roomData.setEntityId(topicRoom.getId());
                                addedRoomIds.add(topicRoom.getId());
                                return roomData;
                            }).collect(() -> expandRoomDatas, List::add)
                            .subscribe();
                });

        Observable.from(teamInfoLoader.getTopicList())
                .filter(TopicRoom::isJoined)
                .filter(topicRoom -> !addedRoomIds.contains(topicRoom.getId()))
                .map(topicRoom -> {

                    ExpandRoomData roomData = new ExpandRoomData();
                    roomData.setEntityId(topicRoom.getId());
                    roomData.setIsStarred(topicRoom.isStarred());
                    roomData.setIsPublicTopic(topicRoom.isPublicTopic());
                    roomData.setName(topicRoom.getName());

                    return roomData;
                }).toSortedList((rhs, lhs) -> StringCompareUtil.compare(rhs.getName(), lhs.getName()))
                .subscribe(expandRoomDatas1 -> {
                    if (expandRoomDatas1.size() > 0) {
                        expandRoomDatas1.get(0).setIsFirstAmongNoFolderItem(true);
                    }
                    expandRoomDatas.addAll(expandRoomDatas1);
                }, Throwable::printStackTrace);

        return expandRoomDatas;
    }

    @Background
    void getDirectMessages() {
        showProgress();

        boolean guest = teamInfoLoader.getMyLevel() == Level.Guest;
        List<ExpandRoomData> userRoomDatas =
                Observable.defer(() -> {
                    if (!guest) {
                        return Observable.from(teamInfoLoader.getUserList());
                    } else {
                        return Observable.from(teamInfoLoader.getTopicList())
                                .filter(TopicRoom::isJoined)
                                .flatMap(topicRoom -> Observable.from(topicRoom.getMembers()))
                                .distinct()
                                .map(memberId -> teamInfoLoader.getUser(memberId));
                    }
                })
                        .filter(User::isEnabled)
                        .filter(user -> user.getId() != teamInfoLoader.getMyId())
                        .map(user -> {
                            ExpandRoomData expandRoomData = new ExpandRoomData();
                            expandRoomData.setEntityId(user.getId());
                            expandRoomData.setIsUser(true);
                            expandRoomData.setName(user.getName());
                            expandRoomData.setProfileUrl(user.getPhotoUrl());
                            return expandRoomData;
                        })
                        .toSortedList((lhs, rhs) -> {
                            if (TeamInfoLoader.getInstance().isJandiBot(lhs.getEntityId())) {
                                return -1;
                            } else if (TeamInfoLoader.getInstance().isJandiBot(rhs.getEntityId())) {
                                return 1;
                            }
                            String lhsName = lhs.getName();
                            String rhsName = rhs.getName();

                            return StringCompareUtil.compare(lhsName, rhsName);
                        })
//                        .collect((Func0<ArrayList<ExpandRoomData>>) ArrayList::new, ArrayList::addToggledUser)
                        .toBlocking()
                        .firstOrDefault(new ArrayList<>());

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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void hideProgress() {
        if (progressWheel == null || !progressWheel.isShowing()) {
            return;
        }
        progressWheel.dismiss();
    }

    @UiThread
    void showError(String message) {
        ColoredToast.showError(message);
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
    public void onItemClick(long roomId, String roomName, int roomType) {
        hideProgress();
        ShareSelectRoomEvent event = new ShareSelectRoomEvent();
        event.setRoomId(roomId);
        event.setRoomName(roomName);
        event.setRoomType(roomType);
        EventBus.getDefault().post(event);
        finish();
    }

}
