package com.tosslab.jandi.app.ui.maintab.topics.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topics.adapter.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topics.model.MainTopicModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
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

    @Background
    public void onInitList() {
        view.showList(mainTopicModel.getDataProvider());
    }

    @Background
    public void onRefreshList() {
        view.refreshList(mainTopicModel.getDataProvider());
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

        mainTopicModel.resetBadge(item.getEntityId());
        int badgeCount = JandiPreference.getBadgeCount(JandiApplication.getContext()) - item.getUnreadCount();
        JandiPreference.setBadgeCount(JandiApplication.getContext(), badgeCount);
        BadgeUtils.setBadge(JandiApplication.getContext(), badgeCount);

        boolean isBadge = hasAlarmCount(
                Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(isBadge));

        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        int teamId = AccountRepository.getRepository().getSelectedTeamInfo()
                .getTeamId();
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());

//        int selectedEntity = item.getEntityId();
//        view.setSelectedItem(selectedEntity);

//        EventBus.getDefault().post(new MainSelectTopicEvent(selectedEntity));

        view.notifyDatasetChanged();
    }

    public void onChildItemLongClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        int entityId = ((ExpandableTopicAdapter) adapter).getTopicItemData(groupPosition, childPosition).getEntityId();
        int folderId = ((ExpandableTopicAdapter) adapter).getTopicFolderData(groupPosition).getFolderId();
        view.showEntityMenuDialog(entityId, folderId);
    }

    @Background
    public void onDeleteTopicFolder(int folderId) {
        mainTopicModel.deleteTopicFolder(folderId);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        onRefreshList();
    }

    @Background
    public void onRenameFolder(int folderId, String name) {
        mainTopicModel.renameFolder(folderId, name);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        onRefreshList();
    }

    public boolean hasAlarmCount(Observable<TopicItemData> joinEntities) {
        TopicItemData defaultValue = new TopicItemData();
        TopicItemData first = joinEntities.filter(topic -> topic.getUnreadCount() > 0)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();
        return first != defaultValue;
    }

    public void onNewMessage(SocketMessageEvent event) {

        if (mainTopicModel.isMe(event.getWriter())) {
            return;
        }

        List<TopicItemData> joinedTopics = view.getJoinedTopics();

        mainTopicModel.updateMessageCount(event, joinedTopics);
        view.updateGroupBadgeCount();
        view.notifyDatasetChanged();

        boolean isBadge = hasAlarmCount(Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(isBadge));
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
    }

}
