package com.bupt.zhidian.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@TableName("stocks")
public class Stock implements Serializable {
    @Id
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
    @TableField("market")
    private String market;

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getMarket() {
        return market;
    }
}