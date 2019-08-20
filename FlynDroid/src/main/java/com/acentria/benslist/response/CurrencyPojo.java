package com.acentria.benslist.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrencyPojo {
    @SerializedName("Key")
    @Expose
    private String currencytype;


    public String getCurrencytype() {
        return currencytype;
    }

    public void setCurrencytype(String currencytype) {
        this.currencytype = currencytype;
    }
}
