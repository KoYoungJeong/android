package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdateSeqFolder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 9. 8..
 */
@EBean
public class TopicFolderSettingModel {

    @Bean
    EntityClientManager entityClientManager;

    public void deleteTopicFolder(int folderId) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        RequestApiManager.getInstance().deleteFolderByTeamApi(teamId, folderId);
    }

    public void modifyFolder(int folderId, String name, int seq) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        ReqUpdateFolder reqUpdateFolder = new ReqUpdateFolder();
        reqUpdateFolder.updateItems = new ReqUpdateFolder.UpdateItems();
        reqUpdateFolder.updateItems.setName(name);
        reqUpdateFolder.updateItems.setSeq(seq);
        RequestApiManager.getInstance().updateFolderByTeamApi(teamId, folderId, reqUpdateFolder);
    }

    public void modifySeqFolder(int folderId, int seq) throws RetrofitError {
        int teamId = entityClientManager.getSelectedTeamId();
        ReqUpdateSeqFolder reqUpdateFolderSeq = new ReqUpdateSeqFolder();
        reqUpdateFolderSeq.updateItems = new ReqUpdateSeqFolder.UpdateSeqItems();
        reqUpdateFolderSeq.updateItems.setSeq(seq);
        RequestApiManager.getInstance().updateFolderByTeamApi(teamId, folderId, reqUpdateFolderSeq);
    }
}
