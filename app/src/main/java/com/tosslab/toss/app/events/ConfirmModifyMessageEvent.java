package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 31..
 * 대화상자에서 메시지 수정의 확인 버튼을 눌렀을 때, 호출자로 돌아가는 event
 */
public class ConfirmModifyMessageEvent {
    public int messageId;
    public String inputMessage;

    public ConfirmModifyMessageEvent(int messageId, String inputMessage) {
        this.messageId = messageId;
        this.inputMessage = inputMessage;
    }
}
