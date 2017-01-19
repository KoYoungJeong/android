package com.tosslab.jandi.app.utils.dynamicl10n;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatKey;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;

import java.io.IOException;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DynamicMessageUtil {

    public static String getMessage(String key) {
        FormatKey formatKey = FormatKey.findFormatKey(key);
        if (formatKey != null) {
            return JandiApplication.getContext().getString(formatKey.getResId());
        } else {
            return "";
        }
    }

    public static Observable<String> getMessageObservable(String key) {
        return FormatKey.findFormatKeyObservable(key)
                .map(it -> JandiApplication.getContext().getString(it.getResId()))
                .defaultIfEmpty("")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static FormatParam getFormatParam(String key, Map formatParamMap) {
        try {
            String rawFormatParam = JsonMapper.getInstance().getObjectMapper().writeValueAsString(formatParamMap);
            return getFormatParam(key, rawFormatParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new FormatParam();
    }

    public static FormatParam getFormatParam(String key, String rawFormatParam) {
        FormatKey formatKey = FormatKey.findFormatKey(key);

        FormatParam param = null;
        if (formatKey != null) {
            try {
                param = JsonMapper.getInstance().getObjectMapper().readValue(rawFormatParam, formatKey.getKlass());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (param == null) {
            param = new FormatParam();
        }

        param.setTypOf(key);
        return param;
    }
}
