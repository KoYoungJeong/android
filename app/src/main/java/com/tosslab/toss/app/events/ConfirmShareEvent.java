package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 6. 26..
 */
public class ConfirmShareEvent {
    public int selectedCdpIdToBeShared;

    public ConfirmShareEvent(int selectedCdpIdToBeShared) {
        this.selectedCdpIdToBeShared = selectedCdpIdToBeShared;
    }
}
