package com.bupt.zhidian.pojo;

import java.math.BigInteger;
import java.util.Date;

public class Share {
    BigInteger share;
    Date date;

    public void setDate(Date date) {
        this.date = date;
    }

    public void setShare(BigInteger share) {
        this.share = share;
    }

    public Date getDate() {
        return date;
    }

    public BigInteger getShare() {
        return share;
    }
}
