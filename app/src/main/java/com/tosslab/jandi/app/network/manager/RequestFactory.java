package com.tosslab.jandi.app.network.manager;

import org.androidannotations.annotations.EBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Created by tonyjs on 15. 5. 20..
 */
@EBean
public class RequestFactory extends SimpleClientHttpRequestFactory {
    public RequestFactory() {
        super();
        setReadTimeout(1000 * 10);
        setConnectTimeout(1000 * 10);
    }
}
