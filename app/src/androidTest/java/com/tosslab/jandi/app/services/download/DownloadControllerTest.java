package com.tosslab.jandi.app.services.download;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.tosslab.jandi.app.services.download.domain.DownloadFileInfo;
import com.tosslab.jandi.app.services.download.model.DownloadModel;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by tonyjs on 15. 11. 19..
 */
@RunWith(AndroidJUnit4.class)
public class DownloadControllerTest {
    @Test
    public void testIsValidateArguments() throws Exception {
        // When
        boolean isValidArguments = DownloadModel.isValidateArguments(
                new DownloadFileInfo(DownloadService.NONE_FILE_ID, "1", "1", "1", "1"));

        // Then
        assertEquals(false, isValidArguments);
    }

    @Test
    public void testMakeDirIfNotExistsAndGet() throws Exception {
        // When
        File dir = DownloadModel.makeDirIfNotExistsAndGet();

        File dirForCheck = Environment.getExternalStoragePublicDirectory("/Jandi");

        // Then
        assertEquals(true, (dirForCheck != null && dir.exists() && dir.isDirectory()));
    }

    @Test
    public void testGetDownloadUrl() throws Exception {
        // When
        String url = "http://www.naver.com";
        String downloadUrl = DownloadModel.getDownloadUrl(url);

        // Then
        assertEquals(true, (!TextUtils.isEmpty(downloadUrl) && downloadUrl.lastIndexOf("/download") > 0));
    }

    @Ignore // 통합 테스트시 URL 인식 못함
    @Test
    public void testDownloadFile() throws Exception {
        // Given
        Context context = InstrumentationRegistry.getContext();

        DownloadController.View view = mock(DownloadController.View.class);
        when(view.getProgressNotificationBuilder(anyInt(), anyString()))
                .thenReturn(new NotificationCompat.Builder(context));
        DownloadController downloadController = new DownloadController(view);

        File dir = DownloadModel.makeDirIfNotExistsAndGet();

        String fileName = "heh_heh_redo__by_a_dawg13-d5acuoq.gif";
        File downloadTargetFile = DownloadModel.getDownloadTargetFile(dir, fileName, "gif");

        String downloadUrl =
                "http://orig05.deviantart.net/89a2/f/2012/220/7/2/heh_heh_redo__by_a_dawg13-d5acuoq.gif";

//        File file = downloadController.downloadFileAndGet(downloadTargetFile, downloadUrl,
//                null);
//        assertEquals(true, (file != null && file.exists()));
    }

    @Ignore
    @Test
    public void testGetDownloadTargetFileWhenDuplicated() throws Exception {

        String fileName = "heh_heh_redo__by_a_dawg13-d5acuoq.gif";
        String fileName2 = "heh_heh_redo__by_a_dawg13-d5acuoq(1).gif";
        try {
            File dir = DownloadModel.makeDirIfNotExistsAndGet();
            for (File file : dir.listFiles()) {
                file.delete();
            }
            File testFile = new File(dir, fileName);
            testFile.createNewFile();


            // When
            File downloadTargetFile = DownloadModel.getDownloadTargetFile(dir, fileName, "gif");

            // Then
            System.out.println(downloadTargetFile.getName());
            assertEquals(true, (!downloadTargetFile.exists() && downloadTargetFile.getName().equals(fileName2)));
        } finally {
            File dir = DownloadModel.makeDirIfNotExistsAndGet();
            new File(dir, fileName).delete();
            new File(dir, fileName2).delete();
        }


    }
}