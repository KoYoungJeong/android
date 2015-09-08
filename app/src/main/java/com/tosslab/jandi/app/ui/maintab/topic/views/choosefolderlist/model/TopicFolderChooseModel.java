package com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 8. 31..
 */

@EBean
public class TopicFolderChooseModel {

    @Bean
    EntityClientManager entityClientManager;

    public List<ResFolder> getFolders() throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        return RequestApiManager.getInstance().getFoldersByTeamApi(teamId);
    }

    public void createFolder(String title) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        ReqCreateFolder reqCreateFolder = new ReqCreateFolder();
        reqCreateFolder.setName(title);
        RequestApiManager.getInstance().createFolderByTeamApi(teamId, reqCreateFolder);
    }

    public void deleteItemFromFolder(int folderId, int topicId) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        LogUtil.e("folderId", folderId + "");
        RequestApiManager.getInstance().deleteFolderItemByTeamApi(teamId, folderId, topicId);
    }

    public void addTopicIntoFolder(int folderId, int topicId) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        ReqRegistFolderItem reqRegistFolderItem = new ReqRegistFolderItem();
        reqRegistFolderItem.setItemId(topicId);
        RequestApiManager.getInstance().registFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem);
    }

}
