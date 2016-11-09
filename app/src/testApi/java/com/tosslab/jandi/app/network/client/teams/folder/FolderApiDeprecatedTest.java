package com.tosslab.jandi.app.network.client.teams.folder;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FolderApiDeprecatedTest {

    private FolderApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(FolderApi.Api.class);
    }

    @Test
    public void createFolder() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createFolder(1, new ReqCreateFolder()).execute())).isFalse();
    }

    @Test
    public void deleteFolder() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteFolder(1, 1).execute())).isFalse();
    }

    @Test
    public void updateFolder() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateFolder(1, 1, new ReqUpdateFolder()).execute())).isFalse();
    }

    @Test
    public void registFolderItem() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.registFolderItem(1, 1, new ReqRegistFolderItem()).execute())).isFalse();
    }

    @Test
    public void deleteFolderItem() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteFolderItem(1, 1, 1).execute())).isFalse();
    }


}