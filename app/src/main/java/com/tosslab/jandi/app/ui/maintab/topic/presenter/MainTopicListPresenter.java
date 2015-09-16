package com.tosslab.jandi.app.ui.maintab.topic.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
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
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by tee on 15. 8. 26..
 */

@EBean
public class MainTopicListPresenter {

    @Bean(MainTopicModel.class)
    MainTopicModel mainTopicModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    public void onLoadList() {
        TopicFolderRepository repository = TopicFolderRepository.getRepository();
        List<ResFolderItem> topicFolderItems = repository.getFolderItems();
        List<ResFolder> topicFolders = repository.getFolders();
        List<FolderExpand> folderExpands = repository.getFolderExpands();
        view.showList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems), folderExpands);
    }

    @Background
    public void onInitList() {
        try {
            List<ResFolder> topicFolders = mainTopicModel.getTopicFolders();
            List<ResFolderItem> topicFolderItems = mainTopicModel.getTopicFolderItems();
            mainTopicModel.saveFolderDataInDB(topicFolders, topicFolderItems);
            view.refreshList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void onRefreshList() {
        try {
            List<ResFolder> topicFolders = mainTopicModel.getTopicFolders();
            List<ResFolderItem> topicFolderItems = mainTopicModel.getTopicFolderItems();
            mainTopicModel.saveFolderDataInDB(topicFolders, topicFolderItems);
            view.refreshList(mainTopicModel.getDataProvider(topicFolders, topicFolderItems));
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
        view.startAnimationSelectedItem();
    }

    public void onChildItemClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        ExpandableTopicAdapter topicAdapter = (ExpandableTopicAdapter) adapter;
        TopicItemData item = topicAdapter.getTopicItemData(groupPosition, childPosition);
        if (item == null) {
            return;
        }
        TopicFolderData topicFolderData = topicAdapter.getTopicFolderData(groupPosition);
        topicFolderData.setChildBadgeCnt(topicFolderData.getChildBadgeCnt() - item.getUnreadCount());
        item.setUnreadCount(0);
        adapter.notifyDataSetChanged();

        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        mainTopicModel.resetBadge(item.getEntityId());
        int badgeCount = JandiPreference.getBadgeCount(JandiApplication.getContext()) - item.getUnreadCount();
        JandiPreference.setBadgeCount(JandiApplication.getContext(), badgeCount);
        BadgeUtils.setBadge(JandiApplication.getContext(), badgeCount);

        int unreadCount = getUnreadCount(Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));

        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        int teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
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


    public interface View {
        void showList(TopicFolderListDataProvider topicFolderListDataProvider, List<FolderExpand> folderExpands);

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
    }

}
