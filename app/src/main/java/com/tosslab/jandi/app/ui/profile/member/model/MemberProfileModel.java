package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Observable;

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

    public String uploadProfilePhoto(File file) throws ExecutionException, InterruptedException {

        EntityManager entityManager = EntityManager.getInstance(context);

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/members/" + entityManager.getMe().getId() + "/profile/photo";

        return Ion.with(context)
                .load(HttpPut.METHOD_NAME, requestURL)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(context).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(file.getName()), file)
                .asString()
                .get();
    }

    public com.tosslab.jandi.app.network.models.ResCommon updateProfileName(ReqProfileName reqProfileName) throws JandiNetworkException {
        EntityManager entityManager = EntityManager.getInstance(context);
        return mJandiEntityClient.updateMemberName(entityManager.getMe().getId(), reqProfileName);
    }

    public String[] getAccountEmails() {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        Iterator<String> confirmedEmails = Observable.from(userEmails)
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .map(userEmail -> userEmail.getId())
                .toBlocking()
                .getIterator();


        List<String> emails = new ArrayList<String>();

        while (confirmedEmails.hasNext()) {
            emails.add(confirmedEmails.next());
        }

        int size = emails.size();
        String[] emailArray = new String[size];

        for (int idx = 0; idx < size; idx++) {
            emailArray[idx] = emails.get(idx);
        }

        return emailArray;


    }

    public ResLeftSideMenu.User updateProfileEmail(String email) throws JandiNetworkException {
        EntityManager entityManager = EntityManager.getInstance(context);
        return mJandiEntityClient.updateMemberEmail(entityManager.getMe().getId(), email);
    }
}
