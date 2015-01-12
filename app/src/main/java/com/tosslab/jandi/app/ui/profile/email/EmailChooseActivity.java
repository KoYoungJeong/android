package com.tosslab.jandi.app.ui.profile.email;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.profile.email.model.EmailChooseModel;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EActivity(R.layout.activity_email_choose)
public class EmailChooseActivity extends Activity {

    @Bean
    EmailChoosePresenter emailChoosePresenter;

    @Bean
    EmailChooseModel emailChooseModel;


    @AfterViews
    void initView() {

        setUpActionBar();

        List<AccountEmail> accountEmails = emailChooseModel.getAccountEmails();
        emailChoosePresenter.setEmails(accountEmails);

    }

    private void setUpActionBar() {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));


    }

    @ItemClick(R.id.lv_email_choose)
    void onEmailItemClick(AccountEmail clickedItem) {

        if (!(clickedItem instanceof AccountEmail.DummyEmail)) {

            AccountEmail selectedEmail = emailChoosePresenter.getSelectedEmail();

            if (selectedEmail != null && clickedItem != selectedEmail) {
                selectedEmail.setSelected(!selectedEmail.isSelected());
            }

            clickedItem.setSelected(!clickedItem.isSelected());
            emailChoosePresenter.refreshListView();
        } else {

        }

    }
}
