package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by tee on 2017. 6. 7..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResGooroomeOtp {

    public Data data;
    public String resultCode;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "ResGooroomeOtp{" +
                "data=" + data +
                ", resultCode='" + resultCode + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {

        public RoomUserOtp roomUserOtp;


        public RoomUserOtp getRoomUserOtp() {
            return roomUserOtp;
        }

        public void setRoomUserOtp(RoomUserOtp roomUserOtp) {
            this.roomUserOtp = roomUserOtp;
        }

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

            public String getOtp() {
                return otp;
            }

            public void setOtp(String otp) {
                this.otp = otp;
            }

            @Override
            public String toString() {
                return "RoomUserOtp{" +
                        "otp='" + otp + '\'' +
                        '}';
            }
        }
    }


}
