package com.tosslab.jandi.app.ui.maintab.tabs.topic.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.services.socket.to.MessageReadEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 15. 8. 26..
 */

@EBean
public class MainTopicListPresenter {

    @Bean(MainTopicModel.class)
    MainTopicModel mainTopicModel;

    @Bean
    TopicFolderSettingModel topicFolderChooseModel;

    View view;

    private List<TopicFolder> topicFolders;
    private List<TopicRoom> topicFolderItems;

    public void setView(View view) {
        this.view = view;
    }

    public void onLoadList() {
        topicFolders = mainTopicModel.getTopicFolders();
        topicFolderItems = mainTopicModel.getJoinedTopics();
        view.showList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
    }

    public void refreshList() {
        topicFolders = mainTopicModel.getTopicFolders();
        topicFolderItems = mainTopicModel.getJoinedTopics();
        view.refreshList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
    }

    public void onUpdatedTopicClick(Topic item) {
        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        EventBus.getDefault().post(MessageReadEvent.fromSelf(teamId, item.getUnreadCount()));
        item.setUnreadCount(0);

        mainTopicModel.getUnreadCount()
                .subscribe(unreadCount -> {
                    EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
                });
        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());
    }

    public void onChildItemClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        ExpandableTopicAdapter topicAdapter = (ExpandableTopicAdapter) adapter;
        TopicItemData item = topicAdapter.getTopicItemData(groupPosition, childPosition);
        if (item == null) {
            return;
        }
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        TopicFolderData topicFolderData = topicAdapter.getTopicFolderData(groupPosition);

        int itemsUnreadCount = item.getUnreadCount();
        EventBus.getDefault().post(MessageReadEvent.fromSelf(teamId, itemsUnreadCount));
        topicFolderData.setChildBadgeCnt(topicFolderData.getChildBadgeCnt() - itemsUnreadCount);
        item.setUnreadCount(0);
        adapter.notifyDataSetChanged();

        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);


        getUnreadCount(Observable.from(topicAdapter.getAllTopicItemData()))
                .subscribe(unreadCount -> {
                    EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
                });

        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());
        view.setSelectedItem(item.getEntityId());
        view.notifyDatasetChangedForFolder();
    }

    public void onUpdatedTopicLongClick(Topic item) {
        long entityId = item.getEntityId();
        long folderId = mainTopicModel.findFolderId(entityId);
        view.showEntityMenuDialog(entityId, folderId);
    }


    public void onChildItemLongClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        TopicItemData topicItemData = ((ExpandableTopicAdapter) adapter).getTopicItemData(groupPosition, childPosition);
        if (topicItemData == null) {
            return;
        }
        long entityId = topicItemData.getEntityId();
        long folderId = ((ExpandableTopicAdapter) adapter).getTopicFolderData(groupPosition).getFolderId();
        view.showEntityMenuDialog(entityId, folderId);
    }

    public Observable<Integer> getUnreadCount(Observable<TopicItemData> joinEntities) {
        return joinEntities
                .map(TopicItemData::getUnreadCount)
                .scan((lhs, rhs) -> lhs + rhs)
                .lastOrDefault(0);
    }

    public void onFolderExpand(TopicFolderData topicFolderData) {
        FolderRepository.getInstance()
                .upsertFolderExpands(topicFolderData.getFolderId(), true);
    }

    public void onFolderCollapse(TopicFolderData topicFolderData) {
        FolderRepository.getInstance()
                .upsertFolderExpands(topicFolderData.getFolderId(), false);
    }

    public List<FolderExpand> onGetFolderExpands() {
        return FolderRepository.getInstance().getFolderExpands();
    }

    @Background
    public void createNewFolder(String title) {
        try {
            ResCreateFolder folder = topicFolderChooseModel.createFolder(title);
            Folder folder1 = new Folder();
            folder1.setRooms(new ArrayList<>());
            folder1.setOpened(false);
            folder1.setName(folder.getName());
            folder1.setId(folder.getId());
            folder1.setSeq(folder.getSeq());
            FolderRepository.getInstance().addFolder(TeamInfoLoader.getInstance().getTeamId(), folder1);
            TeamInfoLoader.getInstance().refresh();
            refreshList();
        } catch (RetrofitException e) {
            if (e.getResponseCode() == 40008) {
                view.showAlreadyHasFolderToast();
            }
        }
    }

    public void onRefreshUpdatedTopicList() {
        List<Topic> topicList = new ArrayList<>();
        mainTopicModel.getUpdatedTopicList()
                .concatWith(
                        Observable.just(
                                Arrays.asList(new Topic.Builder()
                                        .name(JandiApplication.getContext().getString(R.string.jandi_entity_unjoined_topic))
                                        .build())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicList::addAll, t -> {
                }, () -> {
                    view.setUpdatedItems(topicList);

                    int unreadCount = getUnreadCountFromUpdatedList(Observable.from(topicList));
                    EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
                });

    }

    private int getUnreadCountFromUpdatedList(Observable<Topic> topicList) {
        final int[] value = {0};
        topicList.filter(topic -> topic.getUnreadCount() > 0)
                .subscribe(topic -> value[0] += topic.getUnreadCount());
        return value[0];
    }

    public void initUpdatedTopicList() {
        List<Topic> topicList = new ArrayList<>();
        mainTopicModel.getUpdatedTopicList()
                .concatWith(
                        Observable.just(
                                Arrays.asList(new Topic.Builder()
                                        .name(JandiApplication.getContext().getString(R.string.jandi_entity_unjoined_topic))
                                        .build())))
                .subscribe(topicList::addAll, t -> {
                }, () -> view.setUpdatedItems(topicList));

    }

    public void onInitViewList() {
        int lastTopicOrderType = JandiPreference.getLastTopicOrderType();
        if (lastTopicOrderType == 0) {
            view.changeTopicSort(false, true);
        } else {
            view.changeTopicSort(true, false);
        }
    }

    public void updateLastTopicOrderType(boolean changeToFolder) {
        if (changeToFolder) {
            JandiPreference.setLastTopicOrderType(0);
        } else {
            JandiPreference.setLastTopicOrderType(1);
        }
    }

    public interface View {
        void changeTopicSort(boolean currentFolder, boolean changeToFolder);

        void setUpdatedItems(List<Topic> topics);

        void showList(TopicFolderListDataProvider topicFolderListDataProvider);

        void refreshList(TopicFolderListDataProvider topicFolderListDataProvider);

        void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long markerLinkId);

        void notifyDatasetChangedForFolder();

        void notifyDatasetChangedForUpdated();

        void showEntityMenuDialog(long entityId, long folderId);

        void showProgressWheel();

        void dismissProgressWheel();

        void updateGroupBadgeCount();

        void setSelectedItem(long selectedEntity);

        void scrollAndAnimateForSelectedItem();

        void setFolderExpansion();

        void showAlreadyHasFolderToast();

    }

}
