package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdateSeqFolder;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by tee on 15. 9. 8..
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
