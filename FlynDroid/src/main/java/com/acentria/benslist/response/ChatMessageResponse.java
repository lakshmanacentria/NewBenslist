package com.acentria.benslist.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatMessageResponse {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("merchant_id")
    @Expose
    private String merchantId;
    @SerializedName("user_message")
    @Expose
    private String userMessage;
    @SerializedName("merchant_message")
    @Expose
    private String merchantMessage;
    @SerializedName("date")
    @Expose
    private String date;

    private String username;
    private String message;
    private boolean IsMine = false;
    private long message_time;
    private String img_url;





//    public ChatMessageResponse(String username, String message, boolean IsMine) {
//        this.username=username;
//        this.message = message;
//        this.IsMine = IsMine;
//    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getMerchantMessage() {
        return merchantMessage;
    }

    public void setMerchantMessage(String merchantMessage) {
        this.merchantMessage = merchantMessage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public void setUserName(String username) {
        this.username = username;
    }

    public String getUser_name() {
        return username;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsMine() {
        return IsMine;
    }

    public void setMine(boolean mine) {
        IsMine = mine;
    }

    public long getMessage_time() {
        return message_time;
    }

    public void setMessage_time(long message_time) {
        this.message_time = message_time;
    }


    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
