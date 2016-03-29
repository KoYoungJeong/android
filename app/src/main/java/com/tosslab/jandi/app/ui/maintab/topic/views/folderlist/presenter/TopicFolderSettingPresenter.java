package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.TopicFolderMainAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.TopicFolderSettingAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tee on 15. 8. 31..
 */

@EBean
public class TopicFolderSettingPresenter {

    @Bean
    TopicFolderSettingModel topicFolderChooseModel;

    @Bean
    com.tosslab.jandi.app.ui.maintab.topic.dialog.model.TopicFolderSettingModel topicFolderSettingModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void onRefreshFolders(long folderId) {
        boolean hasFolder = false;
        List<ResFolder> folders = null;
        if (NetworkCheckUtil.isConnected()) {
            // 네트워크를 통해 가져오기
            try {
                folders = topicFolderChooseModel.getFolders();
            } catch (RetrofitException retrofitError) {
                retrofitError.printStackTrace();
                folders = new ArrayList<>();
            }
        } else {
            // 로컬에서 가져오기
            TopicFolderRepository repository = TopicFolderRepository.getRepository();
            folders = repository.getFolders();
        }

        // 리턴하는 folder의 length=0이더라도 폴더가 1개이고 속해져있는 폴더인 케이스를 식별해야 한다.
        if (folders.size() > 0) {
            hasFolder = true;
        }

        view.showFolderList(folders, hasFolder);
    }

    @Background
    public void onCreateFolers(String title, long folderId) {
        try {
            topicFolderChooseModel.createFolder(title);
            onRefreshFolders(folderId);
        } catch (RetrofitException e) {
            view.showAlreadyHasFolderToast();
            e.printStackTrace();
        }
    }

    @Background
    public void onDeleteItemFromFolder(long folderId, long topicId, String name) {
        try {
            topicFolderChooseModel.deleteItemFromFolder(folderId, topicId);
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
            view.showMoveToFolderToast(name);
            view.finishAcitivty();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
            view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
        }
    }

    public void onItemClick(RecyclerView.Adapter adapter, int position, int type, long originFolderId, long topicId) {
        TopicFolderMainAdapter topicFolderAdapter = (TopicFolderMainAdapter) adapter;
        ResFolder item = topicFolderAdapter.getItem(position);
        view.setCurrentTopicFolderName(item.name);
        switch (type) {
            case TopicFolderSettingAdapter.TYPE_FOLDER_LIST:
                long newfolderId = item.id;
                if (newfolderId != originFolderId) {
                    onAddTopicIntoFolder(newfolderId, topicId, item.name);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.ChooseFolder);
                } else {
                    view.finishAcitivty();
                }
                break;
            case TopicFolderSettingAdapter.TYPE_REMOVE_FROM_FOLDER:
                ResFolder itemById = topicFolderAdapter.getItemById(originFolderId);
                String folderName;
                if (itemById == null) {
                    folderName = "";
                } else {
                    folderName = itemById.name;
                }
                onDeleteItemFromFolder(originFolderId, topicId, folderName);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.RemoveFromThisFolder);
                break;
            case TopicFolderSettingAdapter.TYPE_MAKE_NEW_FOLDER:
                view.showCreateNewFolderDialog();
                break;
        }
    }

    // 순서 및 이름 변경
    @Background
    public void modifyNameFolder(long folderId, String name, int seq) {
        try {
            topicFolderSettingModel.renameFolder(folderId, name, seq);
            onRefreshFolders(folderId);
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
            onRefreshFolders(folderId);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void removeFolder(long folderId) {
        try {
            topicFolderSettingModel.deleteTopicFolder(folderId);
            onRefreshFolders(folderId);
            view.showDeleteFolderToast();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface View {
        void setCurrentTopicFolderName(String name);

        void showFolderList(List<ResFolder> folders, boolean hasFolder);

        void showCreateNewFolderDialog();

        void finishAcitivty();

        void showMoveToFolderToast(String folderName);

        void showRemoveFromFolderToast(String name);

        void showAlreadyHasFolderToast();

        void showFolderRenamedToast();

        void showDeleteFolderToast();

        void showErrorToast(String message);
    }

}
