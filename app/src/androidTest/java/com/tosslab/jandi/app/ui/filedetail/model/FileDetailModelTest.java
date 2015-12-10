package com.tosslab.jandi.app.ui.filedetail.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by jsuch2362 on 15. 11. 18..
 */
@RunWith(AndroidJUnit4.class)
public class FileDetailModelTest {

    private FileDetailModel fileDetailModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        fileDetailModel = FileDetailModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testGetUnsharedEntities() throws Exception {

        ResMessages.FileMessage fileMessage = getFileMessage();
        List<FormattedEntity> unsharedEntities = fileDetailModel.getUnsharedEntities(fileMessage);

        assertThat(unsharedEntities, is(notNullValue()));
        assertThat(unsharedEntities.size(), is(greaterThan(0)));

    }

    private ResMessages.FileMessage getFileMessage() {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = ReqSearchFile.MAX;

        reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = -1;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";
        reqSearchFile.teamId = EntityManager.getInstance().getTeamId();
        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);

        ResMessages.OriginalMessage originalMessage = resSearchFile.files.get(0);

        return ((ResMessages.FileMessage) originalMessage);
    }
}