package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqUpdateProfile {
    public String department;
    public String email;
    public String name;
    public String phoneNumber;
    public String statusMessage;
    public String position;

}
