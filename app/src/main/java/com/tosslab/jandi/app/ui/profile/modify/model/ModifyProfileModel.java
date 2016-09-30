package com.tosslab.jandi.app.ui.profile.modify.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

public class ModifyProfileModel {

    EntityClientManager entityClientManager;

    public ModifyProfileModel() {
        entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
    }

    public User getProfile() throws RetrofitException {
        Human human = entityClientManager.getUserProfile(TeamInfoLoader.getInstance().getMyId());
        return new User(human);
    }

    public Human updateProfile(ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return entityClientManager.updateUserProfile(TeamInfoLoader.getInstance().getMyId(), reqUpdateProfile);
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
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.email = email;
        entityClientManager.updateUserProfile(TeamInfoLoader.getInstance().getMyId(), reqUpdateProfile);
    }

    public boolean isMyId(long id) {
        return TeamInfoLoader.getInstance().getMyId() == id;
    }

    public User getSavedProfile() {

        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());

    }
}
