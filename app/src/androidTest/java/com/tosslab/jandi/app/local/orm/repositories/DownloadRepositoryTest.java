package com.tosslab.jandi.app.local.orm.repositories;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class DownloadRepositoryTest {

    private DownloadRepository downloadRepository;

    @Before
    public void setUp() throws Exception {
        downloadRepository = DownloadRepository.getInstance();

    }

    @Test
    public void testRepository() throws Exception {
        // upsert Test
        boolean upserted = downloadRepository.upsertDownloadInfo(getDownloadInfo());
        assertThat(upserted, is(true));

        // query Test
        List<DownloadInfo> downloadInfosInProgress = downloadRepository.getDownloadInfosInProgress();
        assertThat(downloadInfosInProgress.size(), is(equalTo(1)));

        // update test
        for (DownloadInfo downloadInfosInProgres : downloadInfosInProgress) {
            int updated = downloadRepository.updateDownloadState(downloadInfosInProgres.getNotificationId(), 1);
            assertThat(updated, is(equalTo(1)));
        }

        // delete test
        for (DownloadInfo downloadInfosInProgres : downloadInfosInProgress) {
            int deleted = downloadRepository.deleteDownloadInfo(downloadInfosInProgres.getNotificationId());
            assertThat(deleted, is(equalTo(1)));
        }
    }

    @NonNull
    private DownloadInfo getDownloadInfo() {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setFileName("hahaha");
        downloadInfo.setState(0);
        downloadInfo.setNotificationId(1);
        return downloadInfo;
    }
}