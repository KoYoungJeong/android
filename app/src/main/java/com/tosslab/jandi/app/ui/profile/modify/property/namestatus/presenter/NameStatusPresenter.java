package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.NameChangeFragment;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class NameStatusPresenter {

    private final View view;
    Lazy<ProfileApi> profileApi;
    Lazy<AccountProfileApi> accountProfileApi;
    BehaviorSubject<String> nameSubject;
    CompositeSubscription subscription;


    @Inject
    public NameStatusPresenter(Lazy<ProfileApi> profileApi,
                               Lazy<AccountProfileApi> accountProfileApi,
                               View view) {
        this.profileApi = profileApi;
        this.accountProfileApi = accountProfileApi;
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
        deferUpdateName(newName)
                .doOnNext(pair -> {
                    if (pair.first) {
                        HumanRepository.getInstance().updateHuman(pair.second);
                        TeamInfoLoader.getInstance().refresh();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateFinish());
    }

    public void updateNameForMainAccount(String newName) {
        Observable.defer(() -> {
            try {
                ResAccountInfo resAccountInfo =
                        accountProfileApi.get().changeName(new ReqProfileName(newName));
                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
            return Observable.just(0);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    view.successUpdate();
                });

    }

    public void updateStatus(String newStatus) {
        deferUpdateStatus(newStatus)
                .doOnNext(pair -> {
                    if (pair.first) {
                        HumanRepository.getInstance().updateHuman(pair.second);
                        TeamInfoLoader.getInstance().refresh();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateFinish());

    }

    @NonNull
    protected Observer<Pair<Boolean, Human>> updateFinish() {
        return new Observer<Pair<Boolean, Human>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                view.dismissProgress();

            }

            @Override
            public void onNext(Pair<Boolean, Human> booleanHumanPair) {
                view.successUpdate();
            }
        };
    }


    private Observable<Pair<Boolean, Human>> deferUpdateName(String newName) {
        return Observable.defer(() -> {

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
        });
    }

    @NonNull
    private Observable<Pair<Boolean, Human>> deferUpdateStatus(String newStatus) {
        return Observable.defer(() -> {

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
        });
    }

    public void onInitUserInfo() {
        Observable.defer(() -> {
            User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
            return Observable.just(user);
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    if (view instanceof NameChangeFragment) {
                        view.setContent(user.getName());
                    } else {
                        view.setContent(user.getStatusMessage());
                    }
                });
    }

    public void onInitUserNameForMainAccount() {
        Observable.defer(() -> {
            String name = AccountRepository.getRepository().getAccountInfo().getName();
            return Observable.just(name);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> view.setContent(name));
    }


    public interface View {
        void dismissProgress();

        void setTextCount(int count);

        void successUpdate();

        void setContent(String content);
    }
}
