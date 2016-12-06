package com.tosslab.jandi.app.team.authority;


public enum Level {
    Guest(0), Owner(1), Admin(2), Member(3);

    private final int level;

    Level(int level) {
        this.level = level;
    }

    public static Level valueOf(int level) {
        switch (level) {
            case 0:
                return Guest;
            case 1:
                return Owner;
            case 2:
                return Admin;
            case 3:
            default:
                return Member;
        }
    }
}
