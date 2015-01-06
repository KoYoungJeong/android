package com.tosslab.jandi.app.ui.profile.account.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
@EBean
public class AccountProfileModel {

    private static final Logger log = Logger.getLogger(AccountProfileModel.class);

    @RootContext
    Context context;

    /**
     * get User Default Info & Email Infos...
     *
     * @return
     */
    public String getAccountName() {
        ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(context).getAccountInfo();
        return accountInfo.getName();
    }

    public String getAccountProfileImage() {
        ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(context).getAccountInfo();
        return !TextUtils.isEmpty(accountInfo.getPhotoThumbnailUrl().largeThumbnailUrl) ? accountInfo.getPhotoThumbnailUrl().largeThumbnailUrl : accountInfo.getPhotoUrl();
    }

    public String getAccountPrimaryEmail() {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail.getId();
            }
        }

        return "";
    }

    public List<ResAccountInfo.UserEmail> getAccountEmails() {

        return JandiAccountDatabaseManager.getInstance(context).getUserEmails();
    }

    /**
     * SD카드가 마운트 되어 있는지 확인
     */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }

        return false;
    }

    public String uploadImage(final File filePath, final ProgressDialog progressDialog) throws Exception {

        EntityManager entityManager = ((JandiApplication) context.getApplicationContext()).getEntityManager();

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/settings/profiles/photo";


        return Ion.with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(context).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(filePath.getName()), filePath)
                .asString().get();
    }

    public ResAccountInfo uploadPrimaryEmail(String primaryEmail) throws JandiNetworkException {
        return RequestManager.newInstance(context, PrimaryEmailChangeRequest.create(context, primaryEmail)).request();
    }

    public ResAccountInfo uploadName(String name) throws JandiNetworkException {
        return RequestManager.newInstance(context, AccountNameChangeRequest.create(context, name)).request();
    }

    public void updateAccountInfo(ResAccountInfo resAccountInfo) {
        JandiAccountDatabaseManager databaseManager = JandiAccountDatabaseManager.getInstance(context);
        databaseManager.upsertAccountInfo(resAccountInfo);
        databaseManager.upsertAccountEmail(resAccountInfo.getEmails());

    }

    public void signOut() {
        // Access Token 삭제
        JandiPreference.signOut(context);

        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.remove(JandiConstants.PARSE_CHANNELS);
        parseInstallation.saveInBackground();

        JandiAccountDatabaseManager.getInstance(context).clearAllData();

    }
}
