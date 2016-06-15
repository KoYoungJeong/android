package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

/**
 * Created by tee on 16. 3. 16..
 */

@EBean
public class SetProfileSecondPagePresenter {

    ModifyProfileModel modifyProfileModel;

    private View view;

    @AfterInject
    void initObject() {
        modifyProfileModel = new ModifyProfileModel();
    }

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void requestProfile() {
        view.showProgressWheel();
        try {
            User me = modifyProfileModel.getSavedProfile();
            view.displayProfileInfos(me);
            view.dismissProgressWheel();
        } catch (Exception e) {
            LogUtil.e("get profile failed", e);
            view.dismissProgressWheel();
            view.showFailProfile();
        }
    }

    public void chooseEmail(String email) {
        String[] accountEmails = modifyProfileModel.getAccountEmails();
        view.showEmailChooseDialog(accountEmails, email);
    }

    public void setEmail(String email) {
        String[] accountEmails = modifyProfileModel.getAccountEmails();
        view.setEmail(accountEmails, email);
    }

    @Background
    public void uploadEmail(String email) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        try {
            modifyProfileModel.updateProfileEmail(email);
        } catch (RetrofitException e) {
            view.updateProfileFailed();
        }
    }

    @Background
    public void uploadExtraInfo(
            String department, String position, String phoneNumber, String statusMessage) {

        view.showProgressWheel();

        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.department = department;
        reqUpdateProfile.position = position;
        reqUpdateProfile.phoneNumber = phoneNumber;
        reqUpdateProfile.statusMessage = statusMessage;

        try {
            modifyProfileModel.updateProfile(reqUpdateProfile);
            view.updateProfileSucceed();
        } catch (RetrofitException e) {
            LogUtil.e("get profile failed", e);
            view.updateProfileFailed();
        } finally {
            view.dismissProgressWheel();
            view.finish();
        }

    }

    public interface View {
        void showEmailChooseDialog(String[] accountEmails, String email);

        void showProgressWheel();

        void dismissProgressWheel();

        void showFailProfile();

        void displayProfileInfos(User me);

        void setEmail(String[] accountEmails, String email);

        void showCheckNetworkDialog();

        void updateProfileFailed();

        void updateProfileSucceed();

        void finish();


    }

}
