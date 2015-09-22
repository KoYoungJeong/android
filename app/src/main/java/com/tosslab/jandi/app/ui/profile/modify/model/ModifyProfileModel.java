package com.tosslab.jandi.app.ui.profile.modify.model;

import android.content.Context;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 31..
 */
@EBean
public class ModifyProfileModel {

    @RootContext
    Context context;

    @Bean
    EntityClientManager entityClientManager;

    public ResLeftSideMenu.User getProfile() throws RetrofitError {
        EntityManager entityManager = EntityManager.getInstance();
        return entityClientManager.getUserProfile(entityManager.getMe().getId());
    }

    public ResLeftSideMenu.User updateProfile(ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        EntityManager entityManager = EntityManager.getInstance();
        return entityClientManager.updateUserProfile(entityManager.getMe().getId(), reqUpdateProfile);
    }

    public String uploadProfilePhoto(File file) throws ExecutionException, InterruptedException {

        EntityManager entityManager = EntityManager.getInstance();

        String requestURL
                = JandiConstantsForFlavors.SERVICE_INNER_API_URL + "/members/" + entityManager.getMe().getId() + "/profile/photo";

        return Ion.with(context)
                .load("PUT", requestURL)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(file.getName()), file)
                .asString()
                .get();
    }

    public com.tosslab.jandi.app.network.models.ResCommon updateProfileName(ReqProfileName reqProfileName) throws RetrofitError {
        EntityManager entityManager = EntityManager.getInstance();
        return entityClientManager.updateMemberName(entityManager.getMe().getId(), reqProfileName);
    }

    public String[] getAccountEmails() {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        Iterator<String> confirmedEmails = Observable.from(userEmails)
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .map(ResAccountInfo.UserEmail::getId)
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

    public ResLeftSideMenu.User updateProfileEmail(String email) throws RetrofitError {
        EntityManager entityManager = EntityManager.getInstance();
        return entityClientManager.updateMemberEmail(entityManager.getMe().getId(), email);
    }

    public boolean isMyId(int id) {
        return EntityManager.getInstance().getMe().getId() == id;
    }

    public ResLeftSideMenu.User getSavedProfile(Context context) {

        return EntityManager.getInstance().getMe().getUser();

    }
}
