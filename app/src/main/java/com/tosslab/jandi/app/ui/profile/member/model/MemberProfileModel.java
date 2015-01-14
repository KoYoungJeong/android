package com.tosslab.jandi.app.ui.profile.member.model;

import android.app.ProgressDialog;
import android.content.Context;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.http.client.methods.HttpPut;

import java.io.File;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 31..
 */
@EBean
public class MemberProfileModel {

    @RootContext
    Context context;

    @Bean
    JandiEntityClient mJandiEntityClient;

    public ResLeftSideMenu.User getProfile() throws JandiNetworkException {
        EntityManager entityManager = EntityManager.getInstance(context);
        return mJandiEntityClient.getUserProfile(entityManager.getMe().getId());
    }

    public ResLeftSideMenu.User updateProfile(ReqUpdateProfile reqUpdateProfile) throws JandiNetworkException {
        EntityManager entityManager = EntityManager.getInstance(context);
        return mJandiEntityClient.updateUserProfile(entityManager.getMe().getId(), reqUpdateProfile);
    }

    public String uploadProfilePhoto(final ProgressDialog progressDialog, File file) throws ExecutionException, InterruptedException {

        EntityManager entityManager = ((JandiApplication) context.getApplicationContext()).getEntityManager();

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/members/" + entityManager.getMe().getId() + "/profile/photo";

        return Ion.with(context)
                .load(HttpPut.METHOD_NAME, requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(context).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(file.getName()), file)
                .asString()
                .get();
    }
}
