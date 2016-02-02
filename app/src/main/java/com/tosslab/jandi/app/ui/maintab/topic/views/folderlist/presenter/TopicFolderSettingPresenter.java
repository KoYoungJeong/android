package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
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

import retrofit.RetrofitError;

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
    public void onRefreshFolders(int folderId) {
        boolean hasFolder = false;
        List<ResFolder> folders = null;
        if (NetworkCheckUtil.isConnected()) {
            // 네트워크를 통해 가져오기
            try {
                folders = topicFolderChooseModel.getFolders();
            } catch (RetrofitError retrofitError) {
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
    public void onCreateFolers(String title, int folderId) {
        try {
            topicFolderChooseModel.createFolder(title);
            onRefreshFolders(folderId);
        } catch (RetrofitError e) {
            view.showAlreadyHasFolderToast();
            e.printStackTrace();
        }
    }

    @Background
    public void onDeleteItemFromFolder(long folderId, long topicId) {
        try {
            topicFolderChooseModel.deleteItemFromFolder(folderId, topicId);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
        view.finishAcitivty();
    }

    @Background
    public void onAddTopicIntoFolder(long folderId, long topicId) {
        try {
            topicFolderChooseModel.addTopicIntoFolder(folderId, topicId);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
        view.finishAcitivty();
    }

    public void onItemClick(RecyclerView.Adapter adapter, int position, int type, long folderId, long topicId) {
        TopicFolderMainAdapter topicFolderAdapter = (TopicFolderMainAdapter) adapter;
        view.setCurrentTopicFolderName(topicFolderAdapter.getItemById(position).name);
        switch (type) {
            case TopicFolderSettingAdapter.TYPE_FOLDER_LIST:
                long newfolderId = topicFolderAdapter.getItemById(position).id;
                if (newfolderId != folderId) {
                    onAddTopicIntoFolder(newfolderId, topicId);
                    String name = ((TopicFolderMainAdapter) adapter).getFolders().get(position).name;
                    view.showMoveToFolderToast(name);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.ChooseFolder);
                } else {
                    view.finishAcitivty();
                }
                break;
            case TopicFolderSettingAdapter.TYPE_REMOVE_FROM_FOLDER:
                onDeleteItemFromFolder(folderId, topicId);
                view.showRemoveFromFolderToast();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.RemoveFromThisFolder);
                break;
            case TopicFolderSettingAdapter.TYPE_MAKE_NEW_FOLDER:
                view.showCreateNewFolderDialog();
                break;
        }
    }

    // 순서 및 이름 변경
    @Background
    public void modifyNameFolder(int folderId, String name, int seq) {
        topicFolderSettingModel.modifyFolder(folderId, name, seq);
        onRefreshFolders(folderId);

    }

    // 순서 및 이름 변경
    @Background
    public void modifySeqFolder(int folderId, int seq) {
        topicFolderSettingModel.modifySeqFolder(folderId, seq);
        onRefreshFolders(folderId);
    }

    @Background
    public void removeFolder(int folderId) {
        topicFolderSettingModel.deleteTopicFolder(folderId);
        onRefreshFolders(folderId);
    }

    public interface View {
        void setCurrentTopicFolderName(String name);

        void showFolderList(List<ResFolder> folders, boolean hasFolder);

        void showCreateNewFolderDialog();

        void finishAcitivty();

        void showMoveToFolderToast(String folderName);

        void showRemoveFromFolderToast();

        void showAlreadyHasFolderToast();
    }

}
