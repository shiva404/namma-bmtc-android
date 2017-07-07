package com.intuit.in24hr.namma_bmtc.types;

import java.io.Serializable;

public class ThanksCount implements Serializable {

    private String refToken;
    private Integer thanksCount;

    public ThanksCount() {
    }

    public ThanksCount(String refToken, Integer thanksCount) {

        this.refToken = refToken;
        this.thanksCount = thanksCount;
    }

    public String getRefToken() {
        return refToken;
    }

    public void setRefToken(String refToken) {
        this.refToken = refToken;
    }

    public Integer getThanksCount() {
        return thanksCount;
    }

    public void setThanksCount(Integer thanksCount) {
        this.thanksCount = thanksCount;
    }
}
