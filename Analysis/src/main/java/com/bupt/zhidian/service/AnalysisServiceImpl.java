package com.bupt.zhidian.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService{
    public String getCompanyCS(String market, String code) throws IOException {
        JSONObject bigObject = new JSONObject();
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/CompanySurvey/PageAjax?code="  + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray jbzl = jsonObject.getJSONArray("jbzl");
        JSONArray fxxl = jsonObject.getJSONArray("fxxg");

        bigObject.put("公司名称", jbzl.getJSONObject(0).get("SECURITY_NAME_ABBR"));
        bigObject.put("上市日期", fxxl.getJSONObject(0).get("FOUND_DATE"));
        bigObject.put("发行价格", fxxl.getJSONObject(0).get("ISSUE_PRICE"));
        bigObject.put("经营范围", jbzl.getJSONObject(0).get("BUSINESS_SCOPE"));
        bigObject.put("公司简介", jbzl.getJSONObject(0).get("ORG_PROFILE"));
        bigObject.put("status", 0);

        return bigObject.toString();
    }

    @Override
    public String getComposition(String market, String code) throws IOException {
        JSONObject bigObject = new JSONObject();
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/BusinessAnalysis/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();

        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray gcfx = jsonObject.getJSONArray("zygcfx");
        JSONArray byProduct = new JSONArray();
        JSONArray byArea = new JSONArray();
        JSONArray byIndustry = new JSONArray();

        classify(1, byIndustry, gcfx);
        classify(2, byProduct, gcfx);
        classify(3, byArea, gcfx);

        bigObject.put("按产品", byProduct);
        bigObject.put("按地区", byArea);
        bigObject.put("按行业", byIndustry);
        if(byIndustry.isEmpty()) {
            JSONObject temp = new JSONObject();
            temp.put("名称", "其他");
            temp.put("排行", 1);
            temp.put("营业收入", sum(byArea, "营业收入"));
            temp.put("主营利润", sum(byArea, "主营利润"));
            temp.put("收入比例", 1);
            temp.put("利润比例", 1);
            byIndustry.put(temp);
        }
        bigObject.put("时间", gcfx.getJSONObject(0).get("REPORT_DATE"));
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    public void classify(int type, JSONArray jsonArray, JSONArray data) {
        String date = data.getJSONObject(0).getString("REPORT_DATE");
        for(int i = 0; i < data.length() && date.equals(data.getJSONObject(i).get("REPORT_DATE")); i++) {
            if(data.getJSONObject(i).getInt("MAINOP_TYPE") == type) {
                JSONObject temp = new JSONObject();
                if(data.getJSONObject(i).has("MAIN_BUSINESS_INCOME")) {
                    temp.put("营业收入", data.getJSONObject(i).getBigInteger("MAIN_BUSINESS_INCOME"));
                }
                if(data.getJSONObject(i).has("MBI_RATIO")) {
                    temp.put("收入比例", data.getJSONObject(i).getDouble("MBI_RATIO"));
                }
                if(data.getJSONObject(i).has("MAIN_BUSINESS_RPOFIT")) {
                    temp.put("主营利润", data.getJSONObject(i).getBigInteger("MAIN_BUSINESS_RPOFIT"));
                }
                if(data.getJSONObject(i).has("MBR_RATIO")) {
                    temp.put("利润比例", data.getJSONObject(i).getDouble("MBR_RATIO"));
                }
                temp.put("排行", data.getJSONObject(i).getInt("RANK"));
                temp.put("名称", data.getJSONObject(i).getString("ITEM_NAME"));
                jsonArray.put(temp);
            }
        }
    }
    
    public BigInteger sum(JSONArray jsonArray, String key) {
        BigInteger fuck = BigInteger.valueOf(0);
        for(int i = 0; i < jsonArray.length(); i++) {
            fuck = fuck.add(jsonArray.getJSONObject(i).getBigInteger(key));
        }
        return fuck;
    }

    @Override
    public String getAchievement(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/NewFinanceAnalysis/DBFXAjaxNew?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("nd");

        JSONArray points = new JSONArray();
        for(int i = 0; i < 4; i++) {
            JSONObject temp = new JSONObject();
            temp.put("时间", jsonArray.getJSONObject(i).get("REPORT_TYPE"));
            temp.put("营业总收入", jsonArray.getJSONObject(i).get("TOTAL_OPERATE_INCOME"));
            temp.put("净利润", jsonArray.getJSONObject(i).get("NETPROFIT"));
            points.put(temp);
        }
        JSONObject bigObject = new JSONObject().put("points", points);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getFhsp(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/BonusFinancing/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }
        JSONArray data = jsonObject.getJSONArray("fhyx");

        JSONObject bigObject = new JSONObject();
        JSONArray fhsp = new JSONArray();
        for(int i = 0; i < data.length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("方案", data.getJSONObject(i).getString("IMPL_PLAN_PROFILE"));
            temp.put("公告日期", data.getJSONObject(i).getString("NOTICE_DATE"));
            temp.put("除权日", data.getJSONObject(i).get("EX_DIVIDEND_DATE"));
            if(temp.get("除权日").toString() == "null") {
                temp.put("除权日", "-");
            }
            fhsp.put(temp);
        }
        bigObject.put("fhsp", fhsp);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getCEO(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/CompanyManagement/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }
        JSONArray data1 = jsonObject.getJSONArray("gglb");
        JSONArray data2 = jsonObject.getJSONArray("cgbd");

        JSONArray ceo = new JSONArray();
        JSONArray zjc = new JSONArray();

        for(int i = 0; i < data1.length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("姓名", data1.getJSONObject(i).getString("PERSON_NAME"));
            temp.put("性别", data1.getJSONObject(i).get("SEX"));
            temp.put("年龄", data1.getJSONObject(i).get("AGE"));
            temp.put("职务", data1.getJSONObject(i).get("POSITION"));
            temp.put("持股数", data1.getJSONObject(i).get("HOLD_NUM"));
            if(temp.get("持股数").toString() == "null") {
                temp.put("持股数", "--");
            }
            temp.put("薪酬", data1.getJSONObject(i).get("SALARY"));
            if(temp.get("薪酬").toString() == "null") {
                temp.put("薪酬", "--");
            }
            ceo.put(temp);
        }

        for(int i = 0; i < data2.length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("公告日期", data2.getJSONObject(i).get("END_DATE"));
            temp.put("变动量", data2.getJSONObject(i).get("CHANGE_NUM"));
            temp.put("均价", data2.getJSONObject(i).get("AVERAGE_PRICE"));
            temp.put("姓名", data2.getJSONObject(i).get("HOLDER_NAME"));
            zjc.put(temp);
        }

        JSONObject bigObject = new JSONObject();
        bigObject.put("高管", ceo);
        bigObject.put("增减持", zjc);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getGdgb(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_78);
        Page page1 = webClient.getPage("https://xueqiu.com/people");
        String urlOfXq = "https://stock.xueqiu.com/v5/stock/f10/cn/shareschg.json?symbol=" + market + code
                + "&count=100&extend=true";
        Page page2 = webClient.getPage(urlOfXq);
        WebResponse responseOfXq = page2.getWebResponse();
        String jsonString = responseOfXq.getContentAsString();
        JSONObject jsonObjectOfXq = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObjectOfXq.getJSONObject("data").getJSONArray("items");

        if(jsonArray.length() == 0) {
            JSONObject error = new JSONObject();
            error.put("status", -1);
            error.put("message", "股票代码不合法");
            return error.toString();
        }

        JSONObject bigObject = new JSONObject();
        bigObject.put("总股本", jsonArray.getJSONObject(0).getBigInteger("total_shares"));
        bigObject.put("流通股本", jsonArray.getJSONObject(0).getBigInteger("float_shares"));
        if(jsonArray.length() != 1) {
            bigObject.put("流通股本变动", (jsonArray.getJSONObject(0).getBigInteger("float_shares")
                    .subtract(jsonArray.getJSONObject(1).getBigInteger("float_shares")))
                    .divide(jsonArray.getJSONObject(1).getBigInteger("float_shares")));
        } else {
            bigObject.put("流通股本变动", "--");
        }
        bigObject.put("流通股本占比", jsonArray.getJSONObject(0).getBigInteger("float_shares")
                .divide(jsonArray.getJSONObject(0).getBigInteger("total_shares")));
        if(jsonArray.length() != 1) {
            if (jsonArray.getJSONObject(0).get("limit_shares_limit_ashare").toString() == "null") {
                bigObject.put("限售A股", BigInteger.valueOf(0));
                if (jsonArray.getJSONObject(1).get("limit_shares_limit_ashare").toString() == "null") {
                    bigObject.put("限售A股变动", "--");
                } else {
                    bigObject.put("限售A股变动", BigInteger.valueOf(-1));
                }
            } else {
                bigObject.put("限售A股", jsonArray.getJSONObject(0).get("limit_shares_limit_ashare"));
                if (jsonArray.getJSONObject(1).get("limit_shares_limit_ashare").toString() == "null") {
                    bigObject.put("限售A股变动", "--");
                } else {
                    bigObject.put("限售A股变动", (jsonArray.getJSONObject(0).getBigInteger("limit_shares_limit_ashare")
                            .subtract(jsonArray.getJSONObject(1).getBigInteger("limit_shares_limit_ashare")))
                            .divide(jsonArray.getJSONObject(1).getBigInteger("limit_shares_limit_ashare")));
                }
            }
        } else {
            if (jsonArray.getJSONObject(0).get("limit_shares_limit_ashare").toString() == "null") {
                bigObject.put("限售A股", BigInteger.valueOf(0));
            } else {
                bigObject.put("限售A股", jsonArray.getJSONObject(0).get("limit_shares_limit_ashare"));
            }
            bigObject.put("限售A股变动", "--");
        }

        String urlOfDf = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageAjax?code=" + market + code;
        Page page3 = webClient.getPage(urlOfDf);
        WebResponse responseOfDf = page3.getWebResponse();
        String jsonString1 = responseOfDf.getContentAsString();
        JSONObject jsonObjectOfDf = new JSONObject(jsonString1);

        JSONArray data = jsonObjectOfDf.getJSONArray("gdrs");
        bigObject.put("股东人数", data.getJSONObject(0).get("HOLDER_TOTAL_NUM"));
        bigObject.put("股东人数变动", data.getJSONObject(0).get("TOTAL_NUM_RATIO"));
        bigObject.put("status", 0);

        return bigObject.toString();
    }

    @Override
    public String getSsbk(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/CoreConception/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        List<String> data = new ArrayList<>();
        for(int i = 0; i < jsonObject.getJSONArray("ssbk").length(); i++) {
            data.add(jsonObject.getJSONArray("ssbk").getJSONObject(i).getString("BOARD_NAME"));
        }

        JSONObject bigObject = new JSONObject().put("所属板块", data);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getJgcc(String market, String code) throws IOException, ParseException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Date> dates = new ArrayList<>();
        for(int i = 0; i < jsonObject.getJSONArray("jgcc_date").length(); i++) {
            dates.add(simpleDateFormat.parse(jsonObject.getJSONArray("jgcc_date")
                    .getJSONObject(i).getString("REPORT_DATE")));
        }

        JSONArray jgcc = new JSONArray();
        for(int i = 0; i < dates.size(); i++) {
            String url1 = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageJGCC?code="
                    + market + code + "&date=" + simpleDateFormat.format(dates.get(i));
            Page page1 = webClient.getPage(url1);
            WebResponse response1 = page1.getWebResponse();
            String jsonString1 = response1.getContentAsString();
            JSONObject jsonObject1 = new JSONObject(jsonString1);
            JSONArray jsonArray = jsonObject1.getJSONArray("jgcc");
            JSONObject smallObject = new JSONObject();
            smallObject.put("报告期", simpleDateFormat.format(dates.get(i)));
            JSONArray smallArray = new JSONArray();
            for(int j = 0; j < jsonArray.length(); j++) {
                if(jsonArray.getJSONObject(j).getString("ORG_TYPE").equals("00")) {
                    smallObject.put("机构持股占比", jsonArray.getJSONObject(j).getFloat("ALL_SHARES_RATIO"));
                    continue;
                }
                JSONObject temp = new JSONObject();
                switch (jsonArray.getJSONObject(j).getString("ORG_TYPE")) {
                    case "01":
                        temp.put("持股机构类型", "基金");
                        break;
                    case "02":
                        temp.put("持股机构类型", "QFII");
                        break;
                    case "03":
                        temp.put("持股机构类型", "社保");
                        break;
                    case "04":
                        temp.put("持股机构类型", "券商");
                        break;
                    case "05":
                        temp.put("持股机构类型", "保险");
                        break;
                    case "06":
                        temp.put("持股机构类型", "信托");
                        break;
                    case "07":
                        temp.put("持股机构类型", "其他机构");
                        break;
                }
                temp.put("持股数", jsonArray.getJSONObject(j).get("TOTAL_FREE_SHARES"));
                temp.put("占流通股比", jsonArray.getJSONObject(j).get("TOTAL_SHARES_RATIO"));
                smallArray.put(temp);
            }
            smallObject.put("data", smallArray);
            jgcc.put(smallObject);
        }
        for(int i = 0; i < jgcc.length()-1; i++) {
            jgcc.getJSONObject(i).put("增持比例", String.valueOf(jgcc.getJSONObject(i).getFloat("机构持股占比")
                    - jgcc.getJSONObject(i+1).getFloat("机构持股占比")));
        }
        jgcc.getJSONObject(jgcc.length()-1).put("增持比例", "--");
        JSONObject bigObject = new JSONObject();
        bigObject.put("jgcc", jgcc);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getSdltgdbd(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray data = jsonObject.getJSONArray("sdltgd");
        JSONArray bd = new JSONArray();
        for(int i = 0; i < data.length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("time", data.getJSONObject(i).get("END_DATE"));
            temp.put("股东", data.getJSONObject(i).get("HOLDER_NAME"));
            temp.put("持股", data.getJSONObject(i).get("HOLD_NUM"));
            temp.put("占比", data.getJSONObject(i).get("FREE_HOLDNUM_RATIO"));
            temp.put("变动", data.getJSONObject(i).get("HOLD_NUM_CHANGE"));
            if(data.getJSONObject(i).get("CHANGE_RATIO").toString() != "null") {
                temp.put("比例", data.getJSONObject(i).get("CHANGE_RATIO"));
            } else {
                temp.put("比例", "--");
            }
            bd.put(temp);
        }
        JSONObject bigObject = new JSONObject().put("sdltgdbd", bd);
        bigObject.put("status", 0);

        return bigObject.toString();
    }

    @Override
    public String getQsccjj(String market, String code) throws IOException, ParseException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONArray data = jsonObject.getJSONArray("jjcg_date");
        JSONArray jjcgData = new JSONArray();
        for(int i = 0; i < data.length(); i++) {
            String url1 = "http://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageJJCG?code="
                    + market + code + "&date=" + simpleDateFormat
                    .format(simpleDateFormat.parse(data.getJSONObject(i).getString("REPORT_DATE")));
            Page page1 = webClient.getPage(url1);
            WebResponse response1 = page1.getWebResponse();
            String jsonString1 = response1.getContentAsString();
            JSONObject jsonObject1 = new JSONObject(jsonString1);
            JSONArray jsonArray = jsonObject1.getJSONArray("jjcg");
            JSONObject temp = new JSONObject();
            JSONArray smallData = new JSONArray();
            temp.put("time", data.getJSONObject(i).getString("REPORT_DATE"));
            for(int j = 0; j < jsonArray.length() && j < 10; j++) {
                JSONObject smallObject = new JSONObject();
                smallObject.put("基金名称", jsonArray.getJSONObject(j).get("HOLDER_NAME"));
                smallObject.put("基金代码", jsonArray.getJSONObject(j).get("HOLDER_CODE"));
                smallObject.put("持股", jsonArray.getJSONObject(j).get("TOTAL_SHARES"));
                smallObject.put("占比", jsonArray.getJSONObject(j).get("TOTALSHARES_RATIO"));
                smallData.put(smallObject);
            }
            temp.put("data", smallData);
            jjcgData.put(temp);
        }
        for(int i = 0; i < jjcgData.length() - 1; i++) {
            for(int j = 0; j < jjcgData.getJSONObject(i).getJSONArray("data").length(); j++) {
                BigInteger temp;
                if((temp = searchShares(jjcgData.getJSONObject(i).getJSONArray("data")
                        .getJSONObject(j).getString("基金代码"),
                        jjcgData.getJSONObject(i+1).getJSONArray("data"))).equals(BigInteger.valueOf(0))) {
                    jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j).put("变动", "--");
                    jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j).put("变动百分比", "--");
                } else {
                    jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j).put("变动",
                            jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j)
                                    .getBigInteger("持股").subtract(temp));
                    jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j).put("变动百分比",
                            jjcgData.getJSONObject(i).getJSONArray("data").getJSONObject(j).getBigInteger("变动")
                            .floatValue()/temp.floatValue());
                }
            }
        }
        for(int i = 0; i < jjcgData.getJSONObject(jjcgData.length()-1).getJSONArray("data").length(); i++) {
            jjcgData.getJSONObject(jjcgData.length()-1).getJSONArray("data").getJSONObject(i)
                    .put("变动", "--");
            jjcgData.getJSONObject(jjcgData.length()-1).getJSONArray("data").getJSONObject(i)
                    .put("变动百分比", "--");
        }
        JSONObject bigObject = new JSONObject();
        bigObject.put("jjcg", jjcgData);
        bigObject.put("status", 0);
        return bigObject.toString();
    }

    public BigInteger searchShares(String code, JSONArray jsonArray) {
        for(int i = 0; i < jsonArray.length(); i++) {
            if(jsonArray.getJSONObject(i).getString("基金代码").equals(code)) {
                return jsonArray.getJSONObject(i).getBigInteger("持股");
            }
        }
        return BigInteger.valueOf(0);
    }

    @Override
    public String getZyzb(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/OperationsRequired/PageAjax?code="
                + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONObject zyzb = jsonObject.getJSONArray("zxzb").getJSONObject(0);
        JSONObject zyzb2 = jsonObject.getJSONArray("zxzbOther").getJSONObject(0);

        JSONObject data = new JSONObject();
        data.put("市盈TTM", zyzb2.get("PE_TTM"));
        data.put("每股收益", zyzb.get("EPSJB"));
        data.put("净资产收益率", zyzb.get("ROEJQ"));
        data.put("营业总收入", zyzb.get("TOTAL_OPERATEINCOME"));
        data.put("净利润", zyzb.get("PARENT_NETPROFIT"));
        data.put("总股本", zyzb.get("TOTAL_SHARE"));
        data.put("市净率", zyzb2.get("PB_MRQ_REALTIME"));
        data.put("每股净资产", zyzb.get("BPS"));
        data.put("资产负债率", zyzb.get("ZCFZL"));
        data.put("营收同比", zyzb.get("TOTALOPERATEREVETZ"));
        data.put("净利润同比", zyzb.get("PARENTNETPROFITTZ"));
        data.put("time", zyzb.get("REPORT_TYPE"));
        data.put("status", 0);

        return data.toString();
    }
}
