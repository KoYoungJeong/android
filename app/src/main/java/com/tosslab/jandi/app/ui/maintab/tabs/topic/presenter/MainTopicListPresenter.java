package com.tosslab.jandi.app.ui.maintab.tabs.topic.presenter;

import android.support.annotation.NonNull;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.IMarkerTopicFolderItem;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain._TopicItemData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainTopicListPresenter {

    private MainTopicModel mainTopicModel;
    private View view;
    private TopicFolderSettingModel topicFolderChooseModel;

    @Inject
    MainTopicListPresenter(View view, MainTopicModel mainTopicModel,
                           TopicFolderSettingModel topicFolderChooseModel) {
        this.view = view;
        this.mainTopicModel = mainTopicModel;
        this.topicFolderChooseModel = topicFolderChooseModel;
    }

    public void onLoadFolderList() {
        Observable
                .fromCallable(() -> mainTopicModel.getTopicFolderDatas())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((datas) -> {
                    view.showList(datas);
                }, Throwable::printStackTrace);
    }

    public void initUpdatedTopicList() {
        List<Topic> topicList = new ArrayList<>();
        mainTopicModel.getUpdatedTopicList()
                .compose(addUnjoinedTopicForUpdated())
                .subscribe(topicList::addAll, t -> {
                }, () -> view.setUpdatedItems(topicList));
    }

    public void refreshList() {
        Observable.fromCallable(() -> mainTopicModel.getTopicFolderDatas())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(datas -> {
                    view.refreshList(datas);
                }, Throwable::printStackTrace);
    }

    public void onUpdatedTopicClick(Topic item) {
        AnalyticsValue.Action action = item.isPublic() ? AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        long teamId = TeamInfoLoader.getInstance().getTeamId();

        int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());
    }

    public void onChildItemClick(_TopicItemData item) {
        if (item == null) {
            return;
        }
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        AnalyticsValue.Action action = item.isPublic() ?
                AnalyticsValue.Action.ChoosePublicTopic : AnalyticsValue.Action.ChoosePrivateTopic;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, action);

        int entityType = item.isPublic() ?
                JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                item.getMarkerLinkId());
        view.setSelectedItem(item.getEntityId());
    }


    public void onUpdatedTopicLongClick(Topic item) {
        long entityId = item.getEntityId();
        long folderId = mainTopicModel.findFolderId(entityId);
        view.showEntityMenuDialog(entityId, folderId);
    }

    public void onChildItemLongClick(_TopicItemData topicItemData) {
        if (topicItemData == null) {
            return;
        }
        view.showEntityMenuDialog(topicItemData.getEntityId(), topicItemData.getParentId());
    }

    public void createNewFolder(String title) {
        Completable.fromCallable(() -> {
            ResCreateFolder folder = topicFolderChooseModel.createFolder(title);
            Folder folder1 = new Folder();
            folder1.setOpened(false);
            folder1.setName(folder.getName());
            folder1.setId(folder.getId());
            folder1.setSeq(folder.getSeq());
            FolderRepository.getInstance(TeamInfoLoader.getInstance().getTeamId()).addFolder(folder1);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refreshList, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getResponseCode() == 40008) {
                            view.showAlreadyHasFolderToast();
                        }
                    }
                });
    }

    public void onRefreshUpdatedTopicList() {
        List<Topic> topicList = new ArrayList<>();
        mainTopicModel.getUpdatedTopicList()
                .compose(addUnjoinedTopicForUpdated())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicList::addAll, t -> {
                }, () -> {
                    view.setUpdatedItems(topicList);
                });
    }

    @NonNull
    private Observable.Transformer<List<Topic>, List<Topic>> addUnjoinedTopicForUpdated() {
        return listObservable -> listObservable.concatWith(
                Observable.defer(() -> {
                    if (TeamInfoLoader.getInstance().getMyLevel() != Level.Guest) {
                        return Observable.just(Arrays.asList(new Topic.Builder()
                                .name(JandiApplication.getContext().getString(R.string.jandi_entity_unjoined_topic))
                                .build()));
                    } else {
                        return Observable.empty();
                    }
                }));
    }

    public void onInitViewList() {
        int lastTopicOrderType = JandiPreference.getLastTopicOrderType();
        if (lastTopicOrderType == 0) {
            view.changeTopicSort(false, true);
        } else {
            view.changeTopicSort(true, false);
        }
    }

    public void checkFloatingActionMenu() {
        view.setFloatingActionMenu(TeamInfoLoader.getInstance().getMyLevel() != Level.Guest);
    }

    public interface View {
        void setFloatingActionMenu(boolean showTopicMenus);

        void changeTopicSort(boolean currentFolder, boolean changeToFolder);

        void setUpdatedItems(List<Topic> topics);

        void showList(List<IMarkerTopicFolderItem> topicFolderItems);

        void refreshList(List<IMarkerTopicFolderItem> topicFolderItems);

        void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long markerLinkId);

        void showEntityMenuDialog(long entityId, long folderId);

        void setSelectedItem(long selectedEntity);

        void showAlreadyHasFolderToast();
    }

}
