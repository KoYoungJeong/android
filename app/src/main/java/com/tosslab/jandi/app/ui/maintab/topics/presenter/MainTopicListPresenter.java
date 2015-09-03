package com.tosslab.jandi.app.ui.maintab.topics.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.maintab.topics.adapter.ExpandableTopicAdapter;
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

    @Bean
    EntityClientManager entityClientManager;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void onInitList() {
//        view.showProgressWheel();
        view.showList(mainTopicModel.getDataProvider());
//        view.dismissProgressWheel();
    }

    @Background
    public void onRefreshList() {
//        view.showProgressWheel();
        view.refreshList(mainTopicModel.getDataProvider());
//        view.dismissProgressWheel();
    }

    public void onChildItemClick(RecyclerView.Adapter adapter, int groupPosition, int childPosition) {
        TopicItemData item = ((ExpandableTopicAdapter) adapter).getTopicItemData(groupPosition, childPosition);
        if (item == null) {
            return;
        }
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


    public interface View {
        void showList(TopicFolderListDataProvider topicFolderListDataProvider);

        void refreshList(TopicFolderListDataProvider topicFolderListDataProvider);

        void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId);

        void notifyDatasetChanged();

        void showEntityMenuDialog(int entityId, int folderId);

        List<TopicItemData> getJoinedTopics();

        void showProgressWheel();

        void dismissProgressWheel();
    }

}
