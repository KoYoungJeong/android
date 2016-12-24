package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdateSeqFolder;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import javax.inject.Inject;

import dagger.Lazy;


public class TopicFolderSettingModel {

    EntityClientManager entityClientManager;
    Lazy<FolderApi> folderApi;

    @Inject
    public TopicFolderSettingModel(Lazy<FolderApi> folderApi, EntityClientManager entityClientManager) {
        this.folderApi = folderApi;
        this.entityClientManager = entityClientManager;
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

    public void deleteTopicFolder(long folderId) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        folderApi.get().deleteFolder(teamId, folderId);
    }

    public void renameFolder(long folderId, String name, int seq) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        ReqUpdateFolder reqUpdateFolder = new ReqUpdateFolder();
        reqUpdateFolder.updateItems = new ReqUpdateFolder.UpdateItems();
        reqUpdateFolder.updateItems.setName(name);
        reqUpdateFolder.updateItems.setSeq(seq);
        folderApi.get().updateFolder(teamId, folderId, reqUpdateFolder);
    }

    public void modifySeqFolder(long folderId, int seq) throws RetrofitException {
        long teamId = entityClientManager.getSelectedTeamId();
        ReqUpdateSeqFolder reqUpdateFolderSeq = new ReqUpdateSeqFolder();
        reqUpdateFolderSeq.updateItems = new ReqUpdateSeqFolder.UpdateSeqItems();
        reqUpdateFolderSeq.updateItems.setSeq(seq);
        folderApi.get().updateFolder(teamId, folderId, reqUpdateFolderSeq);
    }
}
