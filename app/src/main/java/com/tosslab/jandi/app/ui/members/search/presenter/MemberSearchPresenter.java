package com.tosslab.jandi.app.ui.members.search.presenter;

import android.util.Log;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.ui.members.search.model.MemberSearchModel;
import com.tosslab.jandi.app.ui.members.search.presenter.base.BaseMemberSearchPresenterImpl;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public class MemberSearchPresenter extends BaseMemberSearchPresenterImpl {

    private final MemberSearchableDataModel memberSearchableDataModel;
    private final MemberSearchPresenter.View memberSearchView;

    @Inject
    public MemberSearchPresenter(MemberSearchModel memberSearchModel,
                                 MemberSearchableDataModel memberSearchableDataModel,
                                 MemberSearchPresenter.View memberSearchView) {
        super(memberSearchModel);
        this.memberSearchableDataModel = memberSearchableDataModel;
        this.memberSearchView = memberSearchView;
    }

    @Override
    public void onInitializeWholeMembers() {
        memberSearchView.showProgress();

        memberSearchModel.getEnabledMembersObservable()
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(members -> {
                    memberSearchView.hideProgress();

                    memberSearchableDataModel.clear();

                    if (members != null && !members.isEmpty()) {
                        memberSearchableDataModel.setInitializedMembers(members);
                        memberSearchableDataModel.addAll(members);
                    }
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    memberSearchView.hideProgress();
                }, memberSearchView::notifyDataSetChanged);
    }

    @Override
    public void onMemberSearched(String query, List<User> members) {
        memberSearchableDataModel.clear();

        if (members == null || members.isEmpty()) {
            memberSearchableDataModel.setEmptySearchedMember(query);
        } else {
            memberSearchableDataModel.addAll(members);
        }

        memberSearchView.notifyDataSetChanged();
    }

    @Override
    public List<User> getInitializedMembers() {
        return memberSearchableDataModel.getInitializedMembers();
    }

    public interface View {
        void showProgress();

        void hideProgress();

        void notifyDataSetChanged();
    }
}
