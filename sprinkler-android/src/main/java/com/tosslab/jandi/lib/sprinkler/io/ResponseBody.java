package com.tosslab.jandi.lib.sprinkler.io;

/**
 * Created by tonyjs on 15. 7. 23..
 */
final class ResponseBody {
    public static final int SUCCESS = 200;
    public static final int FAIL = 400;

    private int status;

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    @Override
    public String toString() {
        return "ResponseBody{" +
                "status=" + status +
                '}';
    }
}
