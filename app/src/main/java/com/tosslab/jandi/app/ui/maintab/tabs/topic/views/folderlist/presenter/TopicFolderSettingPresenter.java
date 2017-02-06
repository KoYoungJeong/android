package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderMainAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderSettingAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TopicFolderSettingPresenter {

    View view;
    TopicFolderSettingModel topicFolderSettingModel;

    @Inject
    public TopicFolderSettingPresenter(View view,
                                       TopicFolderSettingModel topicFolderSettingModel) {
        this.view = view;
        this.topicFolderSettingModel = topicFolderSettingModel;
    }

    public void onRefreshFolders() {

        Observable.fromCallable(() -> TeamInfoLoader.getInstance().getTopicFolders())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .toSortedList((lhs, rhs) -> lhs.getSeq() - rhs.getSeq())
                .subscribe(topicFolders -> {
                    // 리턴하는 folder의 length=0이더라도 폴더가 1개이고 속해져있는 폴더인 케이스를 식별해야 한다.
                    view.showFolderList(topicFolders, topicFolders.size() > 0);
                });

    }

    public void onCreateFolers(String title, long folderId) {
        Completable.fromCallable(() -> {
            ResCreateFolder folder = topicFolderSettingModel.createFolder(title);
            Folder folder1 = new Folder();
            folder1.setOpened(false);
            folder1.setName(folder.getName());
            folder1.setId(folder.getId());
            folder1.setSeq(folder.getSeq());
            FolderRepository.getInstance(TeamInfoLoader.getInstance().getTeamId()).addFolder(folder1);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                }, t -> {
                    t.printStackTrace();
                    view.showAlreadyHasFolderToast();
                });
    }

    public void onDeleteItemFromFolder(long folderId, long topicId, String name) {
        Completable.fromCallable(() -> {
            topicFolderSettingModel.deleteItemFromFolder(folderId, topicId);
            FolderRepository.getInstance().removeTopic(folderId, topicId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                    view.showRemoveFromFolderToast(name);
                    view.finishAcitivty();

                }, t -> {
                    t.printStackTrace();
                    view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
                });
    }

    public void onAddTopicIntoFolder(long folderId, long topicId, String name) {
        Completable.fromCallable(() -> {
            topicFolderSettingModel.addTopicIntoFolder(folderId, topicId);
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            FolderRepository.getInstance().removeTopicOfTeam(teamId, Arrays.asList(topicId));
            FolderRepository.getInstance().addTopic(folderId, topicId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                    view.showMoveToFolderToast(name);
                    view.finishAcitivty();
                }, t -> {
                    t.printStackTrace();
                    view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
                });
    }

    public void onItemClick(RecyclerView.Adapter adapter, int position, int type, long originFolderId, long topicId) {
        TopicFolderMainAdapter topicFolderAdapter = (TopicFolderMainAdapter) adapter;
        TopicFolder item = topicFolderAdapter.getItem(position);
        switch (type) {
            case TopicFolderSettingAdapter.TYPE_FOLDER_LIST:
                long newfolderId = item.getId();
                if (newfolderId != originFolderId) {
                    onAddTopicIntoFolder(newfolderId, topicId, item.getName());
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.ChooseFolder);
                } else {
                    view.finishAcitivty();
                }
                break;
            case TopicFolderSettingAdapter.TYPE_REMOVE_FROM_FOLDER:
                TopicFolder itemById = topicFolderAdapter.getItemById(originFolderId);
                String folderName;
                if (itemById == null) {
                    folderName = "";
                } else {
                    folderName = itemById.getName();
                }
                onDeleteItemFromFolder(originFolderId, topicId, folderName);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.RemoveFromThisFolder);
                break;
            case TopicFolderSettingAdapter.TYPE_MAKE_NEW_FOLDER:
                view.showCreateNewFolderDialog(false);
                break;
        }
    }

    // 순서 및 이름 변경
    public void modifyNameFolder(long folderId, String name, int seq) {
        Completable.fromCallable(() -> {
            topicFolderSettingModel.renameFolder(folderId, name, seq);
            FolderRepository.getInstance().updateFolderName(folderId, name);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                    view.showFolderRenamedToast();

                }, t -> {
                    t.printStackTrace();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getResponseCode() == 40008) {
                            view.showAlreadyHasFolderToast();
                        }
                    }
                });
    }

    // 순서 및 이름 변경
    public void modifySeqFolder(long folderId, int seq) {
        Completable.fromCallable(() -> {
            topicFolderSettingModel.modifySeqFolder(folderId, seq);
            FolderRepository.getInstance().updateFolderSeq(TeamInfoLoader.getInstance().getTeamId(), folderId, seq);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                }, Throwable::printStackTrace);
    }

    public void removeFolder(long folderId) {
        Completable.fromCallable(() -> {
            topicFolderSettingModel.deleteTopicFolder(folderId);
            FolderRepository.getInstance().deleteFolder(folderId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    EventBus.getDefault().post(new TopicFolderRefreshEvent());
                    onRefreshFolders();
                    view.showDeleteFolderToast();
                }, Throwable::printStackTrace);
    }

    public interface View {
        void showFolderList(List<TopicFolder> folders, boolean hasFolder);

        void showCreateNewFolderDialog(boolean fromActionBar);

        void finishAcitivty();

        void showMoveToFolderToast(String folderName);

        void showRemoveFromFolderToast(String name);

        void showAlreadyHasFolderToast();

        void showFolderRenamedToast();

        void showDeleteFolderToast();

        void showErrorToast(String message);
    }

}
