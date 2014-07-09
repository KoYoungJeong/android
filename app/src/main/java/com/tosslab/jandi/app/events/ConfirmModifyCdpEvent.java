package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 31..
 * 대화상자에서 CDP 수정의 확인 버튼을 눌렀을 때, 호출자로 돌아가는 event
 */
public class ConfirmModifyCdpEvent {
    public int cdpType;
    public int cdpId;
    public String inputName;

    public ConfirmModifyCdpEvent(int cdpType, int cdpId, String inputName) {
        this.cdpType = cdpType;
        this.inputName = inputName;
        this.cdpId = cdpId;
    }
}
