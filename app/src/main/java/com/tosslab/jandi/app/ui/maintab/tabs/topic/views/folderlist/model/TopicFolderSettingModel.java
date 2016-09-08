package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by tee on 15. 8. 31..
 */

@EBean
public class TopicFolderSettingModel {

    @Bean
    EntityClientManager entityClientManager;

    @Inject
    Lazy<FolderApi> folderApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public ResCreateFolder createFolder(String title) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        ReqCreateFolder reqCreateFolder = new ReqCreateFolder();
        reqCreateFolder.setName(title);
        return folderApi.get().createFolder(teamId, reqCreateFolder);
    }

    public void deleteItemFromFolder(long folderId, long topicId) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        LogUtil.e("folderId", folderId + "");
        folderApi.get().deleteFolderItem(teamId, folderId, topicId);
    }

    public void addTopicIntoFolder(long folderId, long topicId) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        ReqRegistFolderItem reqRegistFolderItem = new ReqRegistFolderItem();
        reqRegistFolderItem.setItemId(topicId);
        folderApi.get().registFolderItem(teamId, folderId, reqRegistFolderItem);
    }

}
