package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter;


import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class NameStatusPresenter {

    private final View view;
    Lazy<ProfileApi> profileApi;
    private BehaviorSubject<String> nameSubject;
    private CompositeSubscription subscription;


    @Inject
    public NameStatusPresenter(Lazy<ProfileApi> profileApi, View view) {
        this.profileApi = profileApi;
        this.view = view;

        nameSubject = BehaviorSubject.create("");
        subscription = new CompositeSubscription();
        subscription.add(nameSubject.onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .map(name -> !TextUtils.isEmpty(name) ? name.length() : 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setTextCount));

    }

    public void onDestroy() {
        nameSubject.onCompleted();
        subscription.unsubscribe();
    }

    public void onTextChange(String text) {
        nameSubject.onNext(text);
    }

    public void updateName(String newName) {
        Observable.defer(() -> {

            long myId = TeamInfoLoader.getInstance().getMyId();
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            User user = TeamInfoLoader.getInstance().getUser(myId);
            if (TextUtils.equals(newName, user.getName())) {
                return Observable.just(Pair.create(false, (Human) null));
            }

            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.name = newName;
            try {
                Human human = profileApi.get().updateMemberProfile(teamId, myId, reqUpdateProfile);
                return Observable.just(Pair.create(true, human));
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        }).doOnNext(pair -> {
            if (pair.first) {
                HumanRepository.getInstance().updateHuman(pair.second);
                TeamInfoLoader.getInstance().refresh();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(human -> {
                    view.successUpdate();
                }, t -> {});

    }

    public void updateStatus(String newStatus) {
        Observable.defer(() -> {

            long myId = TeamInfoLoader.getInstance().getMyId();
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            User user = TeamInfoLoader.getInstance().getUser(myId);
            if (TextUtils.equals(newStatus, user.getStatusMessage())) {
                return Observable.just(Pair.create(false, (Human) null));
            }

            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.statusMessage = newStatus;
            try {
                Human human = profileApi.get().updateMemberProfile(teamId, myId, reqUpdateProfile);
                return Observable.just(Pair.create(true, human));
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        }).doOnNext(pair -> {
            if (pair.first) {
                HumanRepository.getInstance().updateHuman(pair.second);
                TeamInfoLoader.getInstance().refresh();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(human -> {
                    view.successUpdate();
                }, t -> {});

    }

    public void onInitUserInfo() {
        Observable.defer(() -> {
            User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
            return Observable.just(user);
        }).subscribe(user -> view.setUser(user));
    }

    public interface View {
        void setTextCount(int count);

        void successUpdate();

        void setUser(User user);
    }
}
