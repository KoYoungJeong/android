package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ConfirmCreateCdpEvent {
    public int cdpType;
    public String inputName;

    public ConfirmCreateCdpEvent(int cdpType, String inputName) {
        this.cdpType = cdpType;
        this.inputName = inputName;
    }
}
