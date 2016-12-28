package com.tosslab.jandi.app.ui.profile.email.adapter;

import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;

import java.util.List;

/**
 * Created by tee on 2016. 12. 22..
 */

public interface EmailChooseAdapterDataModel {

    void setAccountEmails(List<AccountEmail> accountEmails);

    int getCount();

    AccountEmail getItem(int position);

}
