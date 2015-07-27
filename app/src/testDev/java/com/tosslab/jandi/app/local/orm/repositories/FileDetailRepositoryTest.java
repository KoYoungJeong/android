package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 27..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class FileDetailRepositoryTest {

    private ResFileDetail fileDetail;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);

        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = 100;

        reqSearchFile.fileType = "all";
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = -1;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";
        reqSearchFile.teamId = AccountRepository.getRepository().getSelectedTeamId();
        ResSearchFile resSearchFile = RequestApiManager.getInstance()
                .searchFileByMainRest(reqSearchFile);

        ResMessages.FileMessage testFile = null;
        for (ResMessages.OriginalMessage file : resSearchFile.files) {
            if (((ResMessages.FileMessage) file).commentCount > 0) {
                testFile = (ResMessages.FileMessage) file;
                break;
            }
        }

        int fileId = testFile.id;
        fileDetail = EntityClientManager_.getInstance_(Robolectric.application).getFileDetail(fileId);

    }

    @Test
    public void testFileDetailRepository() throws Exception {
        ResMessages.FileMessage fileMessage =
                ((ResMessages.FileMessage) Observable.from(fileDetail.messageDetails)
                        .filter(originalMessage -> originalMessage instanceof ResMessages.FileMessage)
                        .toBlocking().first());

        List<FileDetail> fileDetails = new ArrayList<>();

        Observable.from(fileDetail.messageDetails)
                .filter(originalMessage -> !(originalMessage instanceof ResMessages.FileMessage))
                .map(originalMessage -> {
                    FileDetail fileDetail = new FileDetail();
                    fileDetail.setFile(fileMessage);
                    if (originalMessage instanceof ResMessages.CommentMessage) {
                        fileDetail.setComment(((ResMessages.CommentMessage) originalMessage));
                    } else {
                        fileDetail.setSticker(((ResMessages.CommentStickerMessage) originalMessage));
                    }

                    return fileDetail;
                })
                .collect(() -> fileDetails, List::add)
                .subscribe();

        for (FileDetail detail : fileDetails) {
            FileDetailRepository.getRepository().upsertFileDetail(detail);
        }

        List<FileDetail> fileDetail = FileDetailRepository.getRepository().getFileDetail(fileMessage.id);

        assertThat(fileDetail.size(), is(equalTo(fileDetails.size())));
        assertThat(fileDetail.get(0).getFile(), is(notNullValue()));

    }
}