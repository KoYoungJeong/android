package com.tosslab.jandi.app.network.file;


import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.file.body.ProgressCallback;
import com.tosslab.jandi.app.network.file.body.ProgressRequestBody;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
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
                                            String permission,
                                            long teamId,
                                            String comment,
                                            List<MentionObject> mentions,
                                            File file,
                                            ProgressCallback prgoress) {
        ProgressRequestBody fileBody = new ProgressRequestBody(file, prgoress);

        MultipartBody.Part titlePart = MultipartBody.Part.createFormData("title", title);
        MultipartBody.Part entityPart = MultipartBody.Part.createFormData("share", String.valueOf(shareEntity));
        MultipartBody.Part permissionPart = MultipartBody.Part.createFormData("permission", permission);
        MultipartBody.Part teamIdPart = MultipartBody.Part.createFormData("teamId", String.valueOf(teamId));
        MultipartBody.Part commentPart = null;
        MultipartBody.Part mentionsPart = null;

        if (!TextUtils.isEmpty(comment)) {
            commentPart = MultipartBody.Part.createFormData("comment", comment);
        }
        if (mentions != null && !mentions.isEmpty()) {
            try {
                mentionsPart = MultipartBody.Part.createFormData("mentions", JacksonMapper.getInstance().getObjectMapper().writeValueAsString(mentions));
            } catch (Exception e) {
            }
        }
        MultipartBody.Part userFilePart = MultipartBody.Part.createFormData("userFile", file.getName(), fileBody);

        return RetrofitBuilder.getInstanceOfFileUpload().create(Api.class)
                .upload(titlePart, entityPart, permissionPart, teamIdPart, commentPart, mentionsPart, userFilePart);
    }

    public interface Api {

        @POST("inner-api/file")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        @Multipart
        Call<ResUploadedFile> upload(@Part MultipartBody.Part title,
                                     @Part MultipartBody.Part shareEntity,
                                     @Part MultipartBody.Part permission,
                                     @Part MultipartBody.Part teamId,
                                     @Part MultipartBody.Part comment,
                                     @Part MultipartBody.Part mentions,
                                     @Part MultipartBody.Part file);

    }
}
