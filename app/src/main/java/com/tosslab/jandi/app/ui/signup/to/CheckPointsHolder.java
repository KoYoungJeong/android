package com.tosslab.jandi.app.ui.signup.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 24..
 */ // 각 항목의 체크 여부를 가지고 최종 가입 버튼의 활성화 여부 판별
public class CheckPointsHolder {
    public static final int NOT_KNOW = -1;
    public static final int INVALID = 0;
    public static final int VALID = 1;

    public int isVaildPassword;
    public int isVaildName;
    public int isVaildEmail;
    public int didAgreeAll;

}
