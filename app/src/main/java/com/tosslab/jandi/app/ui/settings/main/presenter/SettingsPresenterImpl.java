package com.tosslab.jandi.app.ui.settings.main.presenter;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

@EBean
public class SettingsPresenterImpl implements SettingsPresenter {
    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onSignOut() {
        if (NetworkCheckUtil.isConnected()) {
            view.showSignoutDialog();
        } else {
            view.showCheckNetworkDialog();
        }
    }

    @Background
    @Override
    public void startSignOut() {
        view.showProgressDialog();
        try {

            Context context = JandiApplication.getContext();

            SignOutUtil.removeSignData();

            ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
            MixpanelAccountAnalyticsClient
                    .getInstance(context, accountInfo.getId())
                    .trackAccountSigningOut()
                    .flush()
                    .clear();

            EntityManager entityManager = EntityManager.getInstance();

            MixpanelMemberAnalyticsClient
                    .getInstance(context, entityManager.getDistictId())
                    .trackSignOut()
                    .flush()
                    .clear();

            JandiSocketService.stopService(context);

            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.deleteAll();
            BadgeUtils.clearBadge(context);

            view.showSuccessToast(context.getString(R.string.jandi_message_logout));


        } catch (Exception e) {
        } finally {
            view.dismissProgressDialog();
        }
        view.moveLoginActivity();

    }

    @Override
    public void onSetUpOrientation(String selectedValue) {
        int orientation = SettingsModel.getOrientationValue(selectedValue);
        view.setOrientation(orientation);
        view.setOrientationSummary(selectedValue);

    }

    @Override
    public void onInitViews() {
        String value = PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).getString("setting_orientation", "0");
        onSetUpOrientation(value);
    }
}
