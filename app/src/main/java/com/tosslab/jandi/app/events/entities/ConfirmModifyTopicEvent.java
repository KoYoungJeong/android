package com.tosslab.jandi.app.events.entities;

/**
 * Created by justinygchoi on 2014. 5. 31..
 * 대화상자에서 topic 수정의 확인 버튼을 눌렀을 때, 호출자로 돌아가는 event
 */
public class ConfirmModifyTopicEvent {
    public int topicType;
    public int topicId;
    public String inputName;

    public ConfirmModifyTopicEvent(int topicType, int topicId, String inputName) {
        this.topicType = topicType;
        this.inputName = inputName;
        this.topicId = topicId;
    }
}
