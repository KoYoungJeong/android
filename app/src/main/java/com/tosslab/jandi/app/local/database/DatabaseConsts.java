package com.tosslab.jandi.app.local.database;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class DatabaseConsts {

    public enum Table {
        account, account_email, account_team, account_device
    }

    public enum Account {
        _id, name, updatedAt, tutoredAt, createdAt, loggedAt, activatedAt, notificationTarget, status
    }

    public enum AccountEmail {
        _id, id, is_primary, status, confirmedAt
    }

    public enum AccountTeam {
        _id, teamId, memberId, name
    }

    public enum AccountDevice {
        _id, token, type, badgeCount, subscribe
    }
}
