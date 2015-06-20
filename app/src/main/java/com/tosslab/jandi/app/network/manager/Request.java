package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.utils.JandiNetworkException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..<br/>
 */
@Deprecated
public interface Request<ResObject> {

    ResObject request() throws JandiNetworkException;

}
