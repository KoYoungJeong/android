package com.tosslab.jandi.app.ui.maintab.topic.presenter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 15. 8. 26..
 */

@EBean
public class MainTopicListPresenter {

    @Bean(MainTopicModel.class)
    MainTopicModel mainTopicModel;

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
        onRefreshList(topicFolders, topicFolderItems, false);
        view.showList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
    }

    @Background
    public void onRefreshList(List<ResFolder> inMemTopicFolders, List<ResFolderItem> inMemTopicFolderItems, boolean onlyInMemory) {

        if (onlyInMemory) {
            refreshListView(mainTopicModel.getDataProvider(inMemTopicFolders, inMemTopicFolderItems));
            return;
        }

        Observable.combineLatest(
                Observable.create(new Observable.OnSubscribe<List<ResFolder>>() {
                    @Override
                    public void call(Subscriber<? super List<ResFolder>> subscriber) {
                        try {
                            List<ResFolder> topicFolders = mainTopicModel.getTopicFolders();
                            subscriber.onNext(topicFolders);
                            subscriber.onCompleted();
                        } catch (RetrofitError retrofitError) {
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
                        } catch (RetrofitError retrofitError) {
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

                }).subscribeOn(Schedulers.io())
                .subscribe(data -> {
                    boolean isExecute = data.first;
                    if (isExecute) {
                        TopicFolderListDataProvider provider = (TopicFolderListDataProvider) data.second;
                        refreshListView(provider);
                        view.setFolderExpansion();
                    }
                    //view.startAnimationSelectedItem();
                }, Throwable::printStackTrace);

    }

    public void refreshListView(TopicFolderListDataProvider provider) {
        view.refreshList(provider);
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

        int teamId = EntityManager.getInstance().getTeamId();
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        int badgeCount = badgeCountRepository.findBadgeCountByTeamId(teamId) - itemsUnreadCount;
        if (badgeCount <= 0) {
            badgeCount = 0;
        }
        badgeCountRepository.upsertBadgeCount(EntityManager.getInstance().getTeamId(), badgeCount);
        BadgeUtils.setBadge(JandiApplication.getContext(), badgeCountRepository.getTotalBadgeCount());

        int unreadCount = getUnreadCount(Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));

        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());
        view.setSelectedItem(((ExpandableTopicAdapter) adapter)
                .getTopicItemData(groupPosition, childPosition).getEntityId());
        view.notifyDatasetChanged();
    }

    public void onChildItemLongClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        int entityId = ((ExpandableTopicAdapter) adapter).getTopicItemData(groupPosition, childPosition).getEntityId();
        int folderId = ((ExpandableTopicAdapter) adapter).getTopicFolderData(groupPosition).getFolderId();
        view.showEntityMenuDialog(entityId, folderId);
    }


    public int getUnreadCount(Observable<TopicItemData> joinEntities) {
        final int[] value = {0};
        joinEntities.filter(topicItemData -> topicItemData.getUnreadCount() > 0)
                .subscribe(topicItemData -> value[0] += topicItemData.getUnreadCount());

        return value[0];
    }

    public void onNewMessage(SocketMessageEvent event) {

        if (mainTopicModel.isMe(event.getWriter())) {
            return;
        }

        List<TopicItemData> joinedTopics = view.getJoinedTopics();

        mainTopicModel.updateMessageCount(event, joinedTopics);
        view.updateGroupBadgeCount();
        view.notifyDatasetChanged();

        int unreadCount = getUnreadCount(Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
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

    public interface View {
        void showList(TopicFolderListDataProvider topicFolderListDataProvider);

        void refreshList(TopicFolderListDataProvider topicFolderListDataProvider);

        void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId);

        void notifyDatasetChanged();

        void showEntityMenuDialog(int entityId, int folderId);

        List<TopicItemData> getJoinedTopics();

        void showProgressWheel();

        void dismissProgressWheel();

        void updateGroupBadgeCount();

        void setSelectedItem(int selectedEntity);

        void startAnimationSelectedItem();

        void setFolderExpansion();
    }

}
