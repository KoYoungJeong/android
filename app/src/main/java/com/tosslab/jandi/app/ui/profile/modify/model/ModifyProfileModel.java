package com.tosslab.jandi.app.ui.profile.modify.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 31..
 */
@EBean
public class ModifyProfileModel {

    @Bean
    EntityClientManager entityClientManager;

    public User getProfile() throws RetrofitException {
        return entityClientManager.getUserProfile(TeamInfoLoader.getInstance().getMyId());
    }

    public ResCommon updateProfile(ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return entityClientManager.updateUserProfile(TeamInfoLoader.getInstance().getMyId(), reqUpdateProfile);
    }

    public ResCommon updateProfileName(ReqProfileName reqProfileName) throws RetrofitException {
        return entityClientManager.updateMemberName(TeamInfoLoader.getInstance().getMyId(), reqProfileName);
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

    public void updateProfileEmail(String email) throws RetrofitException {
        entityClientManager.updateMemberEmail(TeamInfoLoader.getInstance().getMyId(), email);
    }

    public boolean isMyId(long id) {
        return TeamInfoLoader.getInstance().getMyId() == id;
    }

    public User getSavedProfile() {

        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());

    }
}
