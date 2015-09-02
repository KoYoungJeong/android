package com.tosslab.jandi.app.ui.maintab.topics.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.maintab.topics.adapter.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topics.model.MainTopicModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

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
    public void refreshList() {
        view.refreshList(mainTopicModel.getDataProvider());
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

//        boolean isBadge = mainTopicModel.hasAlarmCount(
//                Observable.from(view.getJoinedTopics()));
//        EventBus.getDefault().post(new TopicBadgeEvent(isBadge));

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

    public interface View {
        void showList(TopicFolderListDataProvider topicFolderListDataProvider);

        void refreshList(TopicFolderListDataProvider topicFolderListDataProvider);

        void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId);

        void notifyDatasetChanged();

        void showEntityMenuDialog(int entityId, int folderId);
    }

}
