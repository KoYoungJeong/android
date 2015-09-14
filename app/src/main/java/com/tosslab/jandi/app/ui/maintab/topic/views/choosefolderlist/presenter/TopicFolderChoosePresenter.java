package com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.presenter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.model.TopicFolderChooseModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

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
            folders = topicFolderChooseModel.getFolders();
        } else {
            // 로컬에서 가져오기
            TopicFolderRepository repository = TopicFolderRepository.getRepository();
            folders = repository.getFolders();
        }

        // 리턴하는 folder의 length=0이더라도 폴더가 1개이고 속해져있는 폴더인 케이스를 식별해야 한다.
        if (folders.size() > 0) {
            hasFolder = true;
        }

        // 속해져 있는 폴더는 목록에서 보여주지 않기 위해서
        for (int i = folders.size() - 1; i >= 0; i--) {
            if (folders.get(i).id == folderId) {
                //현재 속한 폴더명을 저장하기 위해 (use for toast)
                view.setCurrentTopicFolderName(folders.get(i).name);
                folders.remove(i);
            }
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
    public void onDeleteItemFromFolder(int folderId, int topicId) {
        topicFolderChooseModel.deleteItemFromFolder(folderId, topicId);
        view.finishAcitivty();
    }

    @Background
    public void onAddTopicIntoFolder(int folderId, int topicId) {
        topicFolderChooseModel.addTopicIntoFolder(folderId, topicId);
        view.finishAcitivty();
    }

    public void onItemClick(RecyclerView.Adapter adapter, int position, int type, int folderId, int topicId) {
        TopicFolderChooseAdapter topicFolderChooseAdapter = (TopicFolderChooseAdapter) adapter;
        switch (type) {
            case TopicFolderChooseAdapter.TYPE_FOLDER_LIST:
                int newfolderId = topicFolderChooseAdapter.getItemById(position).id;
                onAddTopicIntoFolder(newfolderId, topicId);
                String name = ((TopicFolderChooseAdapter) adapter).getFolders().get(position).name;
                view.showMoveToFolderToast(name);
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.ChooseFolder);
                break;
            case TopicFolderChooseAdapter.TYPE_REMOVE_FROM_FOLDER:
                onDeleteItemFromFolder(folderId, topicId);
                view.showRemoveFromFolderToast();
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.RemoveFromThisFolder);
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
