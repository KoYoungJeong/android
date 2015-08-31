package com.tosslab.jandi.app.ui.carousel;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FileSizeUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
@EBean
public class CarouselViewerModel {

    public List<ResMessages.FileMessage> searchInitFileList(int teamId, int roomId, int messageId)
            throws RetrofitError {
        return RequestApiManager.getInstance().searchInitImageFileByFileApi(teamId, roomId,
                messageId, 20);
    }

    public List<ResMessages.FileMessage> searchBeforeFileList(int teamId, int roomId, int fileLinkId, int count) {
        return RequestApiManager.getInstance().searchOldImageFileByFileApi(teamId, roomId,
                fileLinkId, count);
    }

    public List<ResMessages.FileMessage> searchAfterFileList(int teamId, int roomId, int fileLinkId, int count) {
        return RequestApiManager.getInstance().searchNewImageFileByFileApi(teamId, roomId,
                fileLinkId, count);
    }

    public File download(String url, String fileName, String ext, ProgressDialog
            progressDialog, Context context) throws ExecutionException, InterruptedException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        return Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .write(new File(dir, FileSizeUtil.getDownloadFileName(fileName, ext)))
                .get();
    }

    public boolean isMediaFile(String fileType) {

        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    public android.net.Uri addGallery(File result, String fileType, Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, result.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, result.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, fileType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, result.getAbsolutePath());

        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public List<CarouselFileInfo> getImageFileConvert(final int entityId,
                                                      List<ResMessages.FileMessage> fileMessages) {
        List<CarouselFileInfo> fileInfos = new ArrayList<CarouselFileInfo>();

        Observable.from(fileMessages)
                .map(fileMessage -> new CarouselFileInfo.Builder()
                        .entityId(entityId)
                        .fileLinkId(fileMessage.id)
                        .fileName(fileMessage.content.name)
                        .fileType(fileMessage.content.type)
                        .fileLinkUrl(BitmapUtil.getOptimizedImageUrl(fileMessage.content))
                        .ext(fileMessage.content.ext)
                        .size(fileMessage.content.size)
                        .fileCreateTime(
                                DateTransformator.getTimeString(fileMessage.createTime))
                        .fileWriter(EntityManager.getInstance()
                                .getEntityNameById(fileMessage.writerId))
                        .create()).collect(() -> fileInfos, List::add)
                .subscribe();

        return fileInfos;
    }

    public void trackDownloadingFile(EntityManager entityManager, CarouselFileInfo fileInfo,
                                     Context context) {
        if (entityManager != null) {
            MixpanelMemberAnalyticsClient mMixpanelMemberAnalyticsClient =
                    MixpanelMemberAnalyticsClient.getInstance(context, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackDownloadFile(fileInfo.getFileType(), fileInfo
                        .getExt(), fileInfo.getSize());
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    public String getFileType(File file, String fileType) {

        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(fileName.substring(idx + 1, fileName.length()).toLowerCase());
        } else {
            return mimeTypeMap.getExtensionFromMimeType(fileType.toLowerCase());
        }
    }

    public int getTeamId(Context context) {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

    public int findLinkPosition(List<CarouselFileInfo> imageFiles, int fileId) {

        CarouselFileInfo defaultValue = new CarouselFileInfo.Builder().create();
        CarouselFileInfo startFile = Observable.from(imageFiles)
                .filter(carouselFileInfo -> carouselFileInfo.getFileLinkId() == fileId)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return imageFiles.indexOf(startFile);

    }
}
