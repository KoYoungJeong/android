package com.tosslab.jandi.app.ui.maintab.topic.model;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFolder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;

/**
 * Created by jsuch2362 on 15. 9. 8..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class MainTopicModelTest {
    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @Test
    public void testCreateTopic_And_Foldering() throws Exception {
        int teamId = AccountRepository.getRepository().getSelectedTeamId();

        ReqCreateFolder reqCreateFolder;

        List<ResFolder> folders = RequestApiManager.getInstance().getFoldersByTeamApi(teamId);

        final boolean[] waitBooleans = new boolean[100];
        int size = folders.size();
        for (int folderIdx = 0; folderIdx < size; folderIdx++) {
            final int folderId = folders.get(folderIdx).id;
            final int folderiii = folderIdx;


            new Thread(new Runnable() {
                @Override
                public void run() {
                    ReqCreateTopic channel;
                    for (int idx = 0; idx < 1000; idx++) {

                        try {
                            channel = new ReqCreateTopic();
                            channel.teamId = teamId;
                            channel.name = String.format("bb : %d : aa : %04d", folderId, idx);
                            ResCommon common = RequestApiManager.getInstance().createChannelByChannelApi(channel);

                            ReqRegistFolderItem reqRegistFolderItem = new ReqRegistFolderItem();
                            reqRegistFolderItem.setItemId(common.id);
                            RequestApiManager.getInstance().registFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem);
                        } catch (RetrofitError retrofitError) {
                            retrofitError.printStackTrace();
                        }
                    }
                    waitBooleans[folderiii] = true;
                }
            }).start();
        }

        Awaitility.await().timeout(10, TimeUnit.MINUTES).until(() -> {
            for (boolean waitBoolean : waitBooleans) {
                if (!waitBoolean) {
                    return false;
                }
            }
            return true;
        });

    }


}