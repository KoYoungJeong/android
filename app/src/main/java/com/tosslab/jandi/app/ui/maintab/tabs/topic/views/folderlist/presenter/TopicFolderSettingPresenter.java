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
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderMainAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderSettingAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.RealmList;


/**
 * Created by tee on 15. 8. 31..
 */

@EBean
public class TopicFolderSettingPresenter {

    @Bean
    TopicFolderSettingModel topicFolderChooseModel;

    @Bean
    com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.TopicFolderSettingModel topicFolderSettingModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void onRefreshFolders() {
        boolean hasFolder = false;
        List<Folder> folders = FolderRepository.getInstance().getFolders(TeamInfoLoader.getInstance().getTeamId());

        // 리턴하는 folder의 length=0이더라도 폴더가 1개이고 속해져있는 폴더인 케이스를 식별해야 한다.
        if (folders.size() > 0) {
            hasFolder = true;
        }

        view.showFolderList(folders, hasFolder);
    }

    @Background
    public void onCreateFolers(String title, long folderId) {
        try {
            ResCreateFolder folder = topicFolderChooseModel.createFolder(title);
            Folder folder1 = new Folder();
            folder1.setRoomIds(new RealmList<>());
            folder1.setOpened(false);
            folder1.setName(folder.getName());
            folder1.setId(folder.getId());
            folder1.setSeq(folder.getSeq());
            FolderRepository.getInstance().addFolder(TeamInfoLoader.getInstance().getTeamId(), folder1);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
        } catch (RetrofitException e) {
            view.showAlreadyHasFolderToast();
            e.printStackTrace();
        }
    }

    @Background
    public void onDeleteItemFromFolder(long folderId, long topicId, String name) {
        try {
            topicFolderChooseModel.deleteItemFromFolder(folderId, topicId);
            FolderRepository.getInstance().removeTopic(folderId, topicId);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            view.showRemoveFromFolderToast(name);
            view.finishAcitivty();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
            view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
        }
    }

    @Background
    public void onAddTopicIntoFolder(long folderId, long topicId, String name) {
        try {
            topicFolderChooseModel.addTopicIntoFolder(folderId, topicId);
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            FolderRepository.getInstance().removeTopicOfTeam(teamId, Arrays.asList(topicId));
            FolderRepository.getInstance().addTopic(folderId, topicId);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            view.showMoveToFolderToast(name);
            view.finishAcitivty();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
            view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
        }
    }

    public void onItemClick(RecyclerView.Adapter adapter, int position, int type, long originFolderId, long topicId) {
        TopicFolderMainAdapter topicFolderAdapter = (TopicFolderMainAdapter) adapter;
        Folder item = topicFolderAdapter.getItem(position);
        view.setCurrentTopicFolderName(item.getName());
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
                Folder itemById = topicFolderAdapter.getItemById(originFolderId);
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
    @Background
    public void modifyNameFolder(long folderId, String name, int seq) {
        try {
            topicFolderSettingModel.renameFolder(folderId, name, seq);
            FolderRepository.getInstance().updateFolderName(folderId, name);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            view.showFolderRenamedToast();
        } catch (RetrofitException e) {
            e.printStackTrace();
            if (e.getResponseCode() == 40008) {
                view.showAlreadyHasFolderToast();
            }
        }
    }

    // 순서 및 이름 변경
    @Background
    public void modifySeqFolder(long folderId, int seq) {
        try {
            topicFolderSettingModel.modifySeqFolder(folderId, seq);
            FolderRepository.getInstance().updateFolderSeq(TeamInfoLoader.getInstance().getTeamId(), folderId, seq);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void removeFolder(long folderId) {
        try {
            topicFolderSettingModel.deleteTopicFolder(folderId);
            FolderRepository.getInstance().deleteFolder(folderId);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            onRefreshFolders();
            view.showDeleteFolderToast();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface View {
        void setCurrentTopicFolderName(String name);

        void showFolderList(List<Folder> folders, boolean hasFolder);

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
