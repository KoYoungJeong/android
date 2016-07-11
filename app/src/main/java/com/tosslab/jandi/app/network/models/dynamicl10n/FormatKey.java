package com.tosslab.jandi.app.network.models.dynamicl10n;

import android.text.TextUtils;

import rx.Observable;

public enum FormatKey {
    POLL_FINISH("poll_finished", PollFinished.class, 0);

    private final String formatKey;
    private final Class<PollFinished> klass;
    private final int defaultResId;

    FormatKey(String formatKey, Class<PollFinished> klass, int defaultResId) {

        this.formatKey = formatKey;
        this.klass = klass;
        this.defaultResId = defaultResId;
    }

    public static FormatKey findFormatKey(String key) {
        for (FormatKey formatKey : values()) {
            if (TextUtils.equals(formatKey.getFormatKey(), key)) {
                return formatKey;
            }
        }

        return null;
    }

    /**
     * 주의 : defaultIfEmpty 처리 해줄 것!
     *
     * @param key 서버에서 지정해준 formatKey
     * @return 매치되는 첫번째 값
     */
    public static Observable<FormatKey> findFormatKeyObservable(String key) {
        return Observable.defer(() -> Observable.from(values()))
                .takeFirst(it -> TextUtils.equals(it.getFormatKey(), key));
    }

    public String getFormatKey() {
        return formatKey;
    }

    public Class<PollFinished> getKlass() {
        return klass;
    }

    public int getResId() {
        return defaultResId;
    }
}
