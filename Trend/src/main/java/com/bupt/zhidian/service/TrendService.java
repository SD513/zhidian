package com.bupt.zhidian.service;

import java.io.IOException;

public interface TrendService {
    String getPageOfTrend1(int market, String code) throws Exception;
    String getPageOfTrend3(int market, String code) throws Exception;
    Float getPrice(int market, String code) throws IOException;
    String getPoints(int market, String code) throws IOException;
}
