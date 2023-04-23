package com.bupt.zhidian.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

public interface FinancialService {
    String getZcfzb(String market, String code) throws IOException;
    String getDbfx(String market, String code) throws IOException;
    String getLrb(String market, String code) throws IOException;
    String getXjllb(String market, String code) throws IOException;
}
