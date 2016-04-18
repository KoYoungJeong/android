package com.tosslab.jandi.app.network.manager.apiexecutor;

/**
 * Created by tee on 15. 6. 20..
 */
public interface Executor<RESULT> {
    RESULT execute() throws Exception;
}
