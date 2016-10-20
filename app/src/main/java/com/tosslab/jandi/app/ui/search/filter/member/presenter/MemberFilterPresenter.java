package com.tosslab.jandi.app.ui.search.filter.member.presenter;

import android.util.Log;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.model.MemberFilterableDataModel;
import com.tosslab.jandi.app.ui.search.filter.member.model.MemberSearchModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 2016. 7. 26..
 */
public class MemberFilterPresenter extends BaseMemberSearchPresenterImpl {

    private final MemberFilterableDataModel memberFilterableDataModel;
    private final View memberFilterView;

    @Inject
    public MemberFilterPresenter(MemberSearchModel model,
                                 MemberFilterableDataModel memberFilterableDataModel,
                                 MemberFilterPresenter.View memberFilterView) {
        super(model);
        this.memberFilterableDataModel = memberFilterableDataModel;
        this.memberFilterView = memberFilterView;
    }

    @Override
    public void onInitializeWholeMembers() {
        memberFilterView.showProgress();

        memberSearchModel.getEnabledMembersObservable()
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(members -> {
                    memberFilterView.hideProgress();

                    memberFilterableDataModel.clear();

                    memberFilterableDataModel.setInitializedMembers(members);
                    memberFilterableDataModel.addAll(members);
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    memberFilterView.hideProgress();
                }, memberFilterView::notifyDataSetChanged);
    }

    @Override
    public void onMemberSearched(String query, List<User> members) {
        memberFilterableDataModel.clear();

        memberFilterableDataModel.addAll(members);

        memberFilterView.notifyDataSetChanged();
    }

    @Override
    public List<User> getInitializedMembers() {
        return memberFilterableDataModel.getInitializedMembers();
    }

    public void initSelectedMemberId(long selectedMemberId) {
        memberFilterableDataModel.setSelectedMemberId(selectedMemberId);
    }

    public interface View {
        void showProgress();

        void hideProgress();

        void notifyDataSetChanged();
    }
}
