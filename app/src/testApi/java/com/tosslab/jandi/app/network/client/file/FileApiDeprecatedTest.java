package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqSearchFile;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FileApiDeprecatedTest {

    private FileApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(FileApi.Api.class);
    }

    @Test
    public void deleteFile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteFile(1,1).execute())).isFalse();
    }

    @Test
    public void searchInitImageFile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchInitImageFile(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void searchOldImageFile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchOldImageFile(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void searchNewImageFile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchNewImageFile(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void searchFile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchFile(new ReqSearchFile()).execute())).isFalse();
        fail("this is deprecated");
    }

    @Test
    public void enableFileExternalLink() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.enableFileExternalLink(1,1,new ReqNull()).execute())).isFalse();
    }

    @Test
    public void disableFileExternalLink() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.disableFileExternalLink(1,1).execute())).isFalse();
    }


}