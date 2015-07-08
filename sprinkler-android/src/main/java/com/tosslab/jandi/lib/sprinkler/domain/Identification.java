package com.tosslab.jandi.lib.sprinkler.domain;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public class Identification {
    private String accountId;
    private String memberId;
    private String token;

    private Identification(String accountId, String memberId, String token) {
        this.accountId = accountId;
        this.memberId = memberId;
        this.token = token;
    }

    public static Identification create(String accountId, String memberId, String token) {
        return new Identification(accountId, memberId, token);
    }

    public Request toRequest() {
        return new Request(accountId, memberId, token);
    }

    public static class Request {
        private String a;
        private String m;
        private String t;

        public Request(String accountId, String memberId, String token) {
            this.a = accountId;
            this.m = memberId;
            this.t = token;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }
    }

}
