package com.tosslab.jandi.app.network.file.body;

import rx.Observable;
import rx.Subscription;

public interface ProgressCallback {
    Subscription callback(Observable<Integer> callback);
}
