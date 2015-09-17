package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 15. 9. 17..
 */
public class LinkPreviewManager {

    private static LinkPreviewManager instance;

    private LinkPreviewManager() {
        previewStatusMap = new HashMap<>();
    }

    private Map<Integer, PreviewStatus> previewStatusMap;

    public static LinkPreviewManager getInstance() {
        if (instance == null) instance = new LinkPreviewManager();
        return instance;
    }

    public boolean setWait(int linkId, PreviewTimeoutListener previewTimeoutListener) {
        if (!previewStatusMap.containsKey(linkId)
                || previewStatusMap.get(linkId) == PreviewStatus.READY) {

            previewStatusMap.put(linkId, PreviewStatus.WAIT);
            Observable.just(1)
                    .delay(15, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> {

                        if (previewStatusMap.containsKey(linkId)
                                && previewStatusMap.get(linkId) != PreviewStatus.WAIT) {
                            return;
                        }

                        if (previewTimeoutListener != null) {
                            if (previewTimeoutListener.onTimeout(linkId)) {
                                previewStatusMap.put(linkId, PreviewStatus.COMPELTE);
                            } else {
                                previewStatusMap.put(linkId, PreviewStatus.ERROR);
                            }
                        } else {
                            previewStatusMap.put(linkId, PreviewStatus.ERROR);
                        }
                    });
            return true;
        }
        return false;
    }

    public void setComplete(int linkId) {
        previewStatusMap.put(linkId, PreviewStatus.COMPELTE);
    }

    public boolean isError(int linkId) {
        return previewStatusMap.containsKey(linkId)
                && previewStatusMap.get(linkId) == PreviewStatus.ERROR;
    }
}
