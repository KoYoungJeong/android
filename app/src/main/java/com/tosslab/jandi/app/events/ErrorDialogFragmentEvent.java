package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 14. 11. 19..
 */
public class ErrorDialogFragmentEvent {
    public int errorMessageResId;
    public ErrorDialogFragmentEvent(int errorMessageResId) {
        this.errorMessageResId = errorMessageResId;
    }
}
