package com.tosslab.jandi.app.services.download;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.jayway.awaitility.Awaitility;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.Callable;

import setup.BaseInitUtil;

import static org.junit.Assert.assertEquals;
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
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        // When
        boolean isValidArguments = downloadController.isValidateArguments(
                DownloadService.NONE_FILE_ID, "1", "1", "1", "1");

        // Then
        assertEquals(false, isValidArguments);
    }

    @Test
    public void testIsNetworkConnected() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        // When
        Context context = InstrumentationRegistry.getContext();
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        int wifiState = wifiManager.getWifiState();
        System.out.println("wifiState = " + wifiState);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int nextWifiState = wifiManager.getWifiState();
                System.out.println("wait... " + nextWifiState);
                return nextWifiState == WifiManager.WIFI_STATE_DISABLED;
            }
        });

        boolean isConnected = downloadController.isNetworkConnected();

        // Then
        assertEquals(false, isConnected);
        BaseInitUtil.turnOnWifi();
    }

    @Test
    public void testMakeDirIfNotExistsAndGet() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        // When
        File dir = downloadController.makeDirIfNotExistsAndGet();

        File dirForCheck = Environment.getExternalStoragePublicDirectory("/Jandi");

        // Then
        assertEquals(true, (dirForCheck != null && dir.exists() && dir.isDirectory()));
    }

    @Test
    public void testGetDownloadUrl() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        // When
        String url = "http://www.naver.com";
        String downloadUrl = downloadController.getDownloadUrl(url);

        // Then
        assertEquals(true, (!TextUtils.isEmpty(downloadUrl) && downloadUrl.lastIndexOf("/download") > 0));
    }

    @Ignore // 통합 테스트시 URL 인식 못함
    @Test
    public void testDownloadFile() throws Exception {
        // Given
        Context context = InstrumentationRegistry.getContext();

        DownloadController.View view = mock(DownloadController.View.class);
        when(view.getProgressNotificationBuilder(anyString()))
                .thenReturn(new NotificationCompat.Builder(context));
        DownloadController downloadController = new DownloadController(view);

        File dir = downloadController.makeDirIfNotExistsAndGet();

        String fileName = "heh_heh_redo__by_a_dawg13-d5acuoq.gif";
        File downloadTargetFile = downloadController.getDownloadTargetFile(dir, fileName, "gif");

        String downloadUrl =
                "http://orig05.deviantart.net/89a2/f/2012/220/7/2/heh_heh_redo__by_a_dawg13-d5acuoq.gif";

        File file = downloadController.downloadFileAndGet(downloadTargetFile, downloadUrl,
                downloadController.getNotificationId(), view.getProgressNotificationBuilder(fileName));
        assertEquals(true, (file != null && file.exists()));
    }

    @Test
    public void testGetDownloadTargetFileWhenDuplicated() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        File dir = downloadController.makeDirIfNotExistsAndGet();
        for (File file : dir.listFiles()) {
            file.delete();
        }
        String fileName = "heh_heh_redo__by_a_dawg13-d5acuoq.gif";
        File testFile = new File(dir, fileName);
        testFile.createNewFile();

        String fileName2 = "heh_heh_redo__by_a_dawg13-d5acuoq(1).gif";

        // When
        File downloadTargetFile = downloadController.getDownloadTargetFile(dir, fileName, "gif");

        // Then
        System.out.println(downloadTargetFile.getName());
        assertEquals(true, (!downloadTargetFile.exists() && downloadTargetFile.getName().equals(fileName2)));
    }
}