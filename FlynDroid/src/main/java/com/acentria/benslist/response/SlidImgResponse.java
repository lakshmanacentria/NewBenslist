package com.acentria.benslist.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SlidImgResponse {


    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Picture")
    @Expose
    private String picture;
    @SerializedName("URL")
    @Expose
    private String uRL;
    @SerializedName("Position")
    @Expose
    private String position;
    @SerializedName("Status")
    @Expose
    private String status;


    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }



    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
