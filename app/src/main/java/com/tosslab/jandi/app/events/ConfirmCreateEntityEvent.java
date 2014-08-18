package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 * 대화상자에서 CDP 생성의 확인 버튼을 눌렀을 때, 호출자로 돌아가는 event
 */
public class ConfirmCreateEntityEvent {
    public int cdpType;
    public String inputName;

    public ConfirmCreateEntityEvent(int entityType, String inputName) {
        this.cdpType = entityType;
        this.inputName = inputName;
    }
}
