package com.tosslab.jandi.app.services.download;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tonyjs on 15. 11. 18..
 */
@RunWith(AndroidJUnit4.class)
public class DownloadServiceTest {

    @Test
    public void testOnHandleIntentWithInvalidateArgs() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController(view);

        // When
        // Intent 가 null 인 경우
//        downloadController.onHandleIntent(null);

        // Intent extra 에 잘못된 Argument 가 포함되거나 없는 경우
        Intent mockIntent = mock(Intent.class);
        when(mockIntent.getIntExtra(DownloadService.KEY_FILE_ID, -1)).thenReturn(-1);
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_NAME)).thenReturn("zxc");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_URL)).thenReturn("zxc");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_EXTENSIONS)).thenReturn("zxc");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_TYPE)).thenReturn("zxc");

        downloadController.onHandleIntent(mockIntent, false);

        // Then
        verify(view).showErrorToast(anyInt());
    }

    @Ignore
    @Test
    public void testOnHandleIntentWithoutNetwork() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController();
        downloadController.setView(view);

        // When
        downloadController.onHandleIntent(new Intent(), false);

        // Then
        verify(view).showErrorToast(anyInt());
    }

    @Ignore // 통합 테스트 과정에서 의도와 다르게 결과가 나옴
    @Test
    public void testOnHandleIntent() throws Exception {
        // Given
        DownloadController.View view = mock(DownloadController.View.class);
        DownloadController downloadController = new DownloadController();
        downloadController.setView(view);

        DownloadController mock = spy(downloadController);
//        when(mock.getDownloadUrl(anyString())).thenReturn("http://orig05.deviantart.net/89a2/f/2012/220/7/2/heh_heh_redo__by_a_dawg13-d5acuoq.gif");

        Intent mockIntent = mock(Intent.class);
        when(mockIntent.getIntExtra(DownloadService.KEY_FILE_ID, -1)).thenReturn(400);
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_NAME)).thenReturn("heh_heh_redo__by_a_dawg13-d5acuoq.gif");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_URL)).thenReturn("http://orig05.deviantart.net/89a2/f/2012/220/7/2/heh_heh_redo__by_a_dawg13-d5acuoq.gif");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_EXTENSIONS)).thenReturn("gif");
        when(mockIntent.getStringExtra(DownloadService.KEY_FILE_TYPE)).thenReturn("image/gif");

        // When
        mock.onHandleIntent(mockIntent, false);

        // Then
        verify(view).cancelNotification(anyInt());
    }

}