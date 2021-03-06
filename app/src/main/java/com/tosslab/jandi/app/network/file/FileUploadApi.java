package com.tosslab.jandi.app.network.file;


import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.file.body.ProgressCallback;
import com.tosslab.jandi.app.network.file.body.ProgressRequestBody;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class FileUploadApi {

    public Call<ResUploadedFile> uploadFile(String title,
                                            long shareEntity,
                                            long teamId,
                                            String comment,
                                            List<MentionObject> mentions,
                                            File file,
                                            ProgressCallback progressCallback) {
        ProgressRequestBody fileBody = new ProgressRequestBody(file, progressCallback);

        MultipartBody.Part titlePart = MultipartBody.Part.createFormData("title", title);
        MultipartBody.Part entityPart = MultipartBody.Part.createFormData("roomId", String.valueOf(shareEntity));
        MultipartBody.Part teamIdPart = MultipartBody.Part.createFormData("teamId", String.valueOf(teamId));
        MultipartBody.Part commentPart = null;
        MultipartBody.Part mentionsPart = null;

        if (!TextUtils.isEmpty(comment)) {
            commentPart = MultipartBody.Part.createFormData("comment", comment);
        }
        if (mentions != null && !mentions.isEmpty()) {
            try {
                mentionsPart = MultipartBody.Part.createFormData("mentions", JsonMapper.getInstance().getObjectMapper().writeValueAsString(mentions));
            } catch (Exception e) {
            }
        }
        MultipartBody.Part userFilePart = MultipartBody.Part.createFormData("userFile", file.getName(), fileBody);

        return InnerApiRetrofitBuilder.getInstanceOfFileUpload().create(Api.class)
                .upload(titlePart, entityPart, teamIdPart, commentPart, mentionsPart, userFilePart);
    }

    public interface Api {
        @POST("inner-api/file")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        @Multipart
        Call<ResUploadedFile> upload(@Part MultipartBody.Part title,
                                     @Part MultipartBody.Part shareEntity,
                                     @Part MultipartBody.Part teamId,
                                     @Part MultipartBody.Part comment,
                                     @Part MultipartBody.Part mentions,
                                     @Part MultipartBody.Part file);
    }

}
