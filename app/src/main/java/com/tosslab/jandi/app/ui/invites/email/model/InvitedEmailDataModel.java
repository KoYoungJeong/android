package com.tosslab.jandi.app.ui.invites.email.model;

import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 30..
 */
public interface InvitedEmailDataModel {

    int add(EmailVO emailVO);

    void add(int position, EmailVO emailVO);

    void remove(int position);

    void clear();

    EmailVO getInvitedEmail(int position);

    List<EmailVO> getInvitedEmailList();

    int updateEmailToInviteSuccessAndGetPosition(String email);

    int updateEmailToInviteFailAndGetPosition(String email);

    EmailVO findEmailVoByEmail(String email);
}
