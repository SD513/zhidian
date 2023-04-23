package com.bupt.zhidian.service;

import java.io.IOException;
import java.text.ParseException;

public interface AnalysisService {
    String getCompanyCS(String market, String code) throws IOException;
    String getComposition(String market, String code) throws IOException;
    String getAchievement(String market, String code) throws IOException;
    String getFhsp(String market, String code) throws IOException;
    String getCEO(String market, String code) throws IOException;
    String getGdgb(String market, String code) throws IOException;
    String getSsbk(String market, String code) throws IOException;
    String getJgcc(String marekt, String code) throws IOException, ParseException;
    String getSdltgdbd(String market, String code) throws IOException;
    String getQsccjj(String market, String code) throws IOException, ParseException;
    String getZyzb(String market, String code) throws IOException;
}
