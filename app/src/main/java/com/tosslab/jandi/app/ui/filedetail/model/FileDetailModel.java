package com.tosslab.jandi.app.ui.filedetail.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailModel {

    private static final Logger logger = Logger.getLogger(FileDetailModel.class);

    @RootContext
    Context context;

    @Bean
    JandiEntityClient jandiEntityClient;

    public ResFileDetail getFileDetailInfo(int fileId) throws JandiNetworkException {

        return jandiEntityClient.getFileDetail(fileId);
    }

    public void shareMessage(int fileId, int entityIdToBeShared) throws JandiNetworkException {
        jandiEntityClient.shareMessage(fileId, entityIdToBeShared);
    }

    public void unshareMessage(int fileId, int entityIdToBeUnshared) throws JandiNetworkException {
        jandiEntityClient.unshareMessage(fileId, entityIdToBeUnshared);
    }

    public void deleteFile(int fileId) throws JandiNetworkException {
        jandiEntityClient.deleteFile(fileId);
    }

    public void sendMessageComment(int fileId, String message) throws JandiNetworkException {
        jandiEntityClient.sendMessageComment(fileId, message);
    }

    public ResLeftSideMenu.User getUserProfile(int userEntityId) throws JandiNetworkException {
        return jandiEntityClient.getUserProfile(userEntityId);
    }

    public File download(String url, String fileName, String fileType, ProgressDialog progressDialog) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        logger.debug("downloadInBackground " + url);

        progressDialog.dismiss();

        return Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .write(new File(dir, fileName))
                .get();
    }
}
