package com.tosslab.jandi.app.ui.maintab.topic.presenter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.folder.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.model.TopicFolderSettingModel;
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
import rx.Subscriber;
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

    private List<ResFolder> topicFolders;
    private List<ResFolderItem> topicFolderItems;

    public void setView(View view) {
        this.view = view;
    }

    public void onLoadList() {
        TopicFolderRepository repository = TopicFolderRepository.getRepository();
        topicFolders = repository.getFolders();
        topicFolderItems = repository.getFolderItems();
        view.showList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
        onRefreshList(topicFolders, topicFolderItems);
    }

    public void refreshList() {
        refreshListView(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
    }

    @Background(serial = "refresh_topic_list")
    public void onRefreshList(List<ResFolder> inMemTopicFolders, List<ResFolderItem> inMemTopicFolderItems) {

        Observable.combineLatest(
                Observable.create(new Observable.OnSubscribe<List<ResFolder>>() {
                    @Override
                    public void call(Subscriber<? super List<ResFolder>> subscriber) {
                        try {
                            List<ResFolder> topicFolders = mainTopicModel.getTopicFolders();
                            subscriber.onNext(topicFolders);
                            subscriber.onCompleted();
                        } catch (RetrofitException retrofitError) {
                            subscriber.onError(retrofitError);
                        }
                    }
                }), Observable.create(new Observable.OnSubscribe<List<ResFolderItem>>() {
                    @Override
                    public void call(Subscriber<? super List<ResFolderItem>> subscriber) {
                        try {
                            List<ResFolderItem> topicFolderItems = mainTopicModel.getTopicFolderItems();
                            subscriber.onNext(topicFolderItems);
                            subscriber.onCompleted();
                        } catch (RetrofitException retrofitError) {
                            subscriber.onError(retrofitError);
                        }
                    }
                }), (resFolders, resFolderItems) -> {
                    if (((inMemTopicFolders == null) && (inMemTopicFolderItems == null))
                            || !(mainTopicModel.isFolderSame(resFolders, inMemTopicFolders)
                            && mainTopicModel.isFolderItemSame(resFolderItems, inMemTopicFolderItems))) {
                        mainTopicModel.saveFolderDataInDB(resFolders, resFolderItems);
                        this.topicFolders = resFolders;
                        this.topicFolderItems = resFolderItems;
                        Pair<Boolean, TopicFolderListDataProvider> dataProviderPair =
                                new Pair<>(true, mainTopicModel.getDataProvider(resFolders, resFolderItems));
                        return dataProviderPair;
                    } else {
                        return Pair.create(false, null);
                    }

                })
                .subscribe(data -> {
                    boolean isExecute = data.first;
                    if (isExecute) {
                        TopicFolderListDataProvider provider = (TopicFolderListDataProvider) data.second;
                        refreshListView(provider);
                        view.setFolderExpansion();
                    }
                }, Throwable::printStackTrace);

    }

    public void refreshListView(TopicFolderListDataProvider provider) {
        if (view != null) {
            view.refreshList(provider);
        }
    }

    public void onUpdatedTopicClick(Topic item) {
        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        item.setUnreadCount(0);
        mainTopicModel.resetBadge(item.getEntityId());

        long teamId = EntityManager.getInstance().getTeamId();
        int unreadCount = mainTopicModel.getUnreadCount();
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
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
        TopicFolderData topicFolderData = topicAdapter.getTopicFolderData(groupPosition);
        int itemsUnreadCount = item.getUnreadCount();
        topicFolderData.setChildBadgeCnt(topicFolderData.getChildBadgeCnt() - itemsUnreadCount);
        item.setUnreadCount(0);
        adapter.notifyDataSetChanged();

        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        mainTopicModel.resetBadge(item.getEntityId());

        long teamId = EntityManager.getInstance().getTeamId();

        int unreadCount = getUnreadCount(Observable.from(topicAdapter.getAllTopicItemData()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));

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

    public int getUnreadCount(Observable<TopicItemData> joinEntities) {
        final int[] value = {0};
        joinEntities.filter(topicItemData -> topicItemData.getUnreadCount() > 0)
                .subscribe(topicItemData -> value[0] += topicItemData.getUnreadCount());

        return value[0];
    }

    public void onNewMessageForFolder(SocketMessageEvent event, List<TopicItemData> joinedTopics) {

        if (mainTopicModel.isMe(event.getWriter())) {
            return;
        }

        mainTopicModel.updateMessageCount(event, joinedTopics);
        view.updateGroupBadgeCount();
        view.notifyDatasetChangedForFolder();

        int unreadCount = getUnreadCount(Observable.from(joinedTopics));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
    }

    public void onNewMessageForUpdated(SocketMessageEvent event, List<Topic> items) {
        if (mainTopicModel.isMe(event.getWriter())) {
            return;
        }

        mainTopicModel.updateMessageCountForUpdated(event, items);
        onRefreshUpdatedTopicList();

    }

    public void onFolderExpand(TopicFolderData topicFolderData) {
        TopicFolderRepository.getRepository()
                .upsertFolderExpands(topicFolderData.getFolderId(), true);
    }

    public void onFolderCollapse(TopicFolderData topicFolderData) {
        TopicFolderRepository.getRepository()
                .upsertFolderExpands(topicFolderData.getFolderId(), false);
    }

    public List<ResFolder> onGetTopicFolders() {
        return topicFolders;
    }

    public List<ResFolderItem> onGetTopicFolderItems() {
        return topicFolderItems;
    }

    public List<FolderExpand> onGetFolderExpands() {
        TopicFolderRepository repository = TopicFolderRepository.getRepository();
        return repository.getFolderExpands();
    }

    @Background
    public void createNewFolder(String title) {
        try {
            topicFolderChooseModel.createFolder(title);
            view.notifyDatasetChangedForFolder();
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
                .subscribe(topicList::addAll, t -> {}, () -> view.setUpdatedItems(topicList));

    }

    public void initUpdatedTopicList() {
        List<Topic> topicList = new ArrayList<>();
        mainTopicModel.getUpdatedTopicList()
                .concatWith(
                        Observable.just(
                                Arrays.asList(new Topic.Builder()
                                        .name(JandiApplication.getContext().getString(R.string.jandi_entity_unjoined_topic))
                                        .build())))
                .subscribe(topicList::addAll, t -> {}, () -> view.setUpdatedItems(topicList));

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
