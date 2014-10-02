package com.tosslab.jandi.app.lists;

/**
 * Created by justinygchoi on 2014. 9. 21..
 */
public class FormattedDummyEntity extends FormattedEntity {
    public static final int INVITED_USER = 0;
    public static final int TITLE_JOINED_CHANNEL = FormattedEntity.TYPE_TITLE_JOINED_CHANNEL;
    public static final int TITLE_UNJOINED_CHANNEL = FormattedEntity.TYPE_TITLE_UNJOINED_CHANNEL;

    private int mDummyType;
    private String mEmail;


    public FormattedDummyEntity(String emailForInvitation) {
        super();
        mDummyType = INVITED_USER;
        mEmail = emailForInvitation;
    }

    public FormattedDummyEntity(int type) {
        super(type);
        mDummyType = type;
    }

    public boolean isInvitedMember() {
        return (mDummyType == INVITED_USER);
    }

    public String getEmailForInvitation() {
        return mEmail;
    }
}
