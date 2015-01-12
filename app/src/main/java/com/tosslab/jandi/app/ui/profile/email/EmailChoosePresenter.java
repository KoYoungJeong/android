package com.tosslab.jandi.app.ui.profile.email;

import android.content.Context;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapter;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EBean
public class EmailChoosePresenter {


    @ViewById(R.id.lv_email_choose)
    ListView emailListView;

    @RootContext
    Context context;

    EmailChooseAdapter emailChooseAdapter;
    private List<AccountEmail> accountEmails;

    @AfterInject
    void initObject() {
        emailChooseAdapter = new EmailChooseAdapter(context);
    }

    @AfterViews
    void initView() {

        emailListView.setAdapter(emailChooseAdapter);

        if (accountEmails != null) {
            emailChooseAdapter.setAccountEmails(accountEmails);
            emailChooseAdapter.notifyDataSetChanged();
        }
    }

    public AccountEmail getSelectedEmail() {

        int count = emailChooseAdapter.getCount();
        for (int idx = 0; idx < count; ++idx) {
            AccountEmail item = emailChooseAdapter.getItem(idx);
            if (item.isSelected()) {
                return item;
            }
        }

        return null;

    }

    public void setEmails(List<AccountEmail> accountEmails) {
        this.accountEmails = accountEmails;
    }

    public void refreshListView() {
        emailChooseAdapter.notifyDataSetChanged();
    }
}
