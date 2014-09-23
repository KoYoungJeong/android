package com.tosslab.jandi.app.lists;

/**
 * Created by justinygchoi on 2014. 9. 21..
 */
public class FormattedDummyEntity extends FormattedEntity {
    public static final int TYPE_INVITED_USER   = 0;
    private int mDummyType;
    private String mEmail;


    public FormattedDummyEntity(String emailForInvitation) {
        super();
        mEmail = emailForInvitation;
    }

    public String getEmailForInvitation() {
        return mEmail;
    }
}
