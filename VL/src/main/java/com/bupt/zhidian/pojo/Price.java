package com.bupt.zhidian.pojo;

import java.util.Date;

public class Price {
    Float price;
    Date date;

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public Float getPrice() {
        return price;
    }
}
