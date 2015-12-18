package com.tosslab.jandi.app.local.orm.repositories;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class FileDetailRepositoryTest {

    private Dao<ResMessages.FileContent, ?> fileContentDao;

    @Before
    public void setUp() throws Exception {
        OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        fileContentDao = helper.getDao(ResMessages.FileContent.class);

    }

    @Test
    public void testUpdateFileExternalLink() throws Exception {
        ResMessages.FileContent data = createFileContent();

        String changedCode = "externalCode2";
        String changedUrl = "externalUrl2";
        boolean changedShared = false;
        FileDetailRepository.getRepository().updateFileExternalLink(data.fileUrl, changedShared, changedUrl, changedCode);

        ResMessages.FileContent fileContent = fileContentDao.queryBuilder().where().eq("fileUrl", data.fileUrl)
                .queryForFirst();

        assertThat(fileContent.externalCode, is(equalTo(changedCode)));
        assertThat(fileContent.externalUrl, is(equalTo(changedUrl)));
        assertThat(fileContent.externalShared, is(changedShared));
    }

    @NonNull
    private ResMessages.FileContent createFileContent() throws java.sql.SQLException {
        fileContentDao.deleteBuilder().delete();
        ResMessages.FileContent data = new ResMessages.FileContent();
        data.fileUrl = new Date().toString();
        data.externalShared = true;
        data.externalUrl = "externalUrl1";
        data.externalCode = "externalCode1";
        fileContentDao.create(data);
        return data;
    }
}