package com.acentria.benslist.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrencyPojo {

    @SerializedName("code")
    @Expose
    private String currencytype;


    public String getCurrencytype() {
        return currencytype;
    }

    public void setCurrencytype(String currencytype) {
        this.currencytype = currencytype;
    }

    public CurrencyPojo(String currencytype) {
        this.currencytype = currencytype;
    }

    @Override
    public String toString() {
        return  currencytype ;
    }

}
