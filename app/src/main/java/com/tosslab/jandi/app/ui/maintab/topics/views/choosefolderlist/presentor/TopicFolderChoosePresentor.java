package com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.presentor;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.model.TopicFolderChooseModel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

/**
 * Created by tee on 15. 8. 31..
 */

@EBean
public class TopicFolderChoosePresentor {

    @Bean
    TopicFolderChooseModel topicFolderChooseModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void onRefreshFolders() {
        List<ResFolder> folders = null;
        if (NetworkCheckUtil.isConnected()) {
            // 네트워크를 통해 가져오기
            folders = topicFolderChooseModel.getFolders();
        } else {
            // 로컬에서 가져오기
            TopicFolderRepository repository = TopicFolderRepository.getRepository();
            folders = repository.getFolders();
        }

        view.showFolderList(folders);
    }

    @Background
    public void onCreateFolers(String title) {
        topicFolderChooseModel.createFolder(title);
        onRefreshFolders();
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
                break;
            case TopicFolderChooseAdapter.TYPE_REMOVE_FROM_FOLDER:
                onDeleteItemFromFolder(folderId, topicId);
                break;
            case TopicFolderChooseAdapter.TYPE_MAKE_NEW_FOLDER:
                view.showCreateNewFolderDialog();
                break;
        }
    }

    public interface View {
        void showFolderList(List<ResFolder> folders);

        void showCreateNewFolderDialog();

        void finishAcitivty();
    }

}
