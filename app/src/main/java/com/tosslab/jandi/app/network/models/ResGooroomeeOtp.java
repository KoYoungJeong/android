package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by tee on 2017. 6. 7..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResGooroomeeOtp {

    public Data data;
    public String description;
    public String resultCode;

    @Override
    public String toString() {
        return "ResGooroomeeOtp{" +
                "data=" + data +
                ", description='" + description + '\'' +
                ", resultCode='" + resultCode + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {

        public RoomUserOtp roomUserOtp;

        @Override
        public String toString() {
            return "Data{" +
                    "roomUserOtp=" + roomUserOtp +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class RoomUserOtp {

            public String otp;
            public int expireIn;

            @Override
            public String toString() {
                return "RoomUserOtp{" +
                        "otp='" + otp + '\'' +
                        ", expireIn=" + expireIn +
                        '}';
            }
        }
    }


}
