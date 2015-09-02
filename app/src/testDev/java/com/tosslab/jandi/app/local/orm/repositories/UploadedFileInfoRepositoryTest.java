package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 15. 9. 2..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class UploadedFileInfoRepositoryTest {

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testInsertAndGet() throws Exception {
        UploadedFileInfo tempFileInfo = new UploadedFileInfo();
        tempFileInfo.setMessageId(1);
        String localPath = "/temp";
        tempFileInfo.setLocalPath(localPath);

        int index = UploadedFileInfoRepository.getRepository().insertFileInfo(tempFileInfo);
        assertThat(index, is(greaterThan(0)));

        UploadedFileInfo uploadedFileInfo = UploadedFileInfoRepository.getRepository().getUploadedFileInfo(1);

        assertThat(uploadedFileInfo.getMessageId(), is(equalTo(1)));
        assertThat(uploadedFileInfo.getLocalPath(), is(equalTo(localPath)));

        uploadedFileInfo = UploadedFileInfoRepository.getRepository().getUploadedFileInfo(0);
        assertThat(uploadedFileInfo.getMessageId(), is(equalTo(0)));
        assertThat(uploadedFileInfo.getLocalPath(), is(equalTo("")));
    }

}