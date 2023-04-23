package com.bupt.zhidian.pojo;

import java.util.Date;

public class Report {
    Date date;
    int profit;

    public void setDate(Date date) {
        this.date = date;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }

    public Date getDate() {
        return date;
    }

    public int getProfit() {
        return profit;
    }
}
