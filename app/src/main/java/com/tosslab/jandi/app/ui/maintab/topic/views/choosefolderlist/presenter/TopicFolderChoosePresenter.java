package com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.model.TopicFolderChooseModel;
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
public class TopicFolderChoosePresenter {

    @Bean
    TopicFolderChooseModel topicFolderChooseModel;

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
        TopicFolderChooseAdapter topicFolderChooseAdapter = (TopicFolderChooseAdapter) adapter;
        switch (type) {
            case TopicFolderChooseAdapter.TYPE_FOLDER_LIST:
                long newfolderId = topicFolderChooseAdapter.getItemById(position).id;
                if (newfolderId != folderId) {
                    onAddTopicIntoFolder(newfolderId, topicId);
                    String name = ((TopicFolderChooseAdapter) adapter).getFolders().get(position).name;
                    view.showMoveToFolderToast(name);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.ChooseFolder);
                } else {
                    view.finishAcitivty();
                }
                break;
            case TopicFolderChooseAdapter.TYPE_REMOVE_FROM_FOLDER:
                onDeleteItemFromFolder(folderId, topicId);
                view.showRemoveFromFolderToast();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.RemoveFromThisFolder);
                break;
            case TopicFolderChooseAdapter.TYPE_MAKE_NEW_FOLDER:
                view.showCreateNewFolderDialog();
                break;
        }
    }

    public interface View {
        void showFolderList(List<ResFolder> folders, boolean hasFolder);

        void showCreateNewFolderDialog();

        void finishAcitivty();

        void showMoveToFolderToast(String folderName);

        void showRemoveFromFolderToast();

        void setCurrentTopicFolderName(String currentItemFolderName);

        void showAlreadyHasFolderToast();
    }

}
