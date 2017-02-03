package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileApiDeprecatedTest {

    private ProfileApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(ProfileApi.Api.class);
    }

    @Test
    public void updateMemberProfile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateMemberProfile(1, 1, new ReqUpdateProfile()))).isFalse();
    }

    @Test
    public void getAvartarsInfo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getAvartarsInfo())).isFalse();
    }

    @Test
    public void getMemberProfile() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getMemberProfile(1,1))).isFalse();
    }

    @Ignore
    @Test
    public void uploadProfilePhoto() throws Exception {
        // Test 불가로 Ignore 상태로 함
        File file = new File("asda");
        MediaType mediaType = MediaType.parse(URLConnection.guessContentTypeFromName(file.getAbsolutePath()));
        MultipartBody.Part userFilePart = MultipartBody.Part.createFormData(file.getName(), "asd", RequestBody.create(mediaType, file));
        assertThat(ValidationUtil.isDeprecated(api.uploadProfilePhoto(1,1,userFilePart)));
    }


}