package com.bupt.zhidian.service;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

import com.mysql.cj.xdevapi.JsonArray;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class FinancialServiceImpl implements FinancialService {
    @Override
    public String getZcfzb(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_78);
        Page page1 = webClient.getPage("https://xueqiu.com/people");
        String urlOfXq = "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol="
                + market + code
                + "&type=all&is_detail=true&count=5";
        Page page2 = webClient.getPage(urlOfXq);
        WebResponse responseOfXq = page2.getWebResponse();
        String jsonString = responseOfXq.getContentAsString();
        JSONObject jsonObjectOfXq = new JSONObject(jsonString);
        JSONArray list = jsonObjectOfXq.getJSONObject("data").getJSONArray("list");

        if(list.length() == 0) {
            JSONObject error = new JSONObject();
            error.put("status", -1);
            error.put("message", "股票代码不合法");
            return error.toString();
        }

        Double shares = Double.parseDouble(getGb(market, code));
        JSONObject dbfx = new JSONObject(getDbfx(market, code));

        JSONArray nd = new JSONArray();
        JSONArray bgq = new JSONArray();

        JSONArray bgqzb = getZyzb(market, code, 0);
        JSONArray ndzb = getZyzb(market, code, 1);

        for(int i = 0; i < 5; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("总资产", list.getJSONObject(i).getJSONArray("total_assets").get(0));
            jsonObject.put("总负债", list.getJSONObject(i).getJSONArray("total_liab").get(0));
            jsonObject.put("资产负债率", jsonObject.getFloat("总负债")/jsonObject.getFloat("总资产"));
            jsonObject.put("流动资产", list.getJSONObject(i).getJSONArray("total_current_assets"));
            jsonObject.put("非流动资产", list.getJSONObject(i).getJSONArray("total_noncurrent_assets"));
            jsonObject.put("商誉", list.getJSONObject(i).getJSONArray("goodwill").get(0));
            jsonObject.put("存货", list.getJSONObject(i).getJSONArray("inventory").get(0));
            jsonObject.put("应收票据及应收账款", list.getJSONObject(i).getJSONArray("account_receivable").get(0));
            jsonObject.put("交易性金融资产", list.getJSONObject(i).getJSONArray("tradable_fnncl_assets").get(0));
            jsonObject.put("固定资产", list.getJSONObject(i).getJSONArray("fixed_asset_sum").get(0));
            jsonObject.put("无形资产", list.getJSONObject(i).getJSONArray("intangible_assets").get(0));
            jsonObject.put("长期股权投资", list.getJSONObject(i).getJSONArray("lt_equity_invest").get(0));
            jsonObject.put("生产性生物资产", list.getJSONObject(i).getJSONArray("productive_biological_assets").get(0));
            if(jsonObject.get("商誉").toString() == "null") {
                jsonObject.put("商誉", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("存货").toString() == "null") {
                jsonObject.put("存货", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("应收票据及应收账款").toString() == "null") {
                jsonObject.put("应收票据及应收账款", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("交易性金融资产").toString() == "null") {
                jsonObject.put("交易性金融资产", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("固定资产").toString() == "null") {
                jsonObject.put("固定资产", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("无形资产").toString() == "null") {
                jsonObject.put("无形资产", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("长期股权投资").toString() == "null") {
                jsonObject.put("长期股权投资", BigDecimal.valueOf(0));
            }
            if(jsonObject.get("生产性生物资产").toString() == "null") {
                jsonObject.put("生产性生物资产", BigDecimal.valueOf(0));
            }
            jsonObject.put("8项资产合计", jsonObject.getBigDecimal("商誉")
                            .add(jsonObject.getBigDecimal("存货"))
                            .add(jsonObject.getBigDecimal("应收票据及应收账款"))
                            .add(jsonObject.getBigDecimal("交易性金融资产"))
                            .add(jsonObject.getBigDecimal("固定资产"))
                            .add(jsonObject.getBigDecimal("无形资产"))
                            .add(jsonObject.getBigDecimal("长期股权投资"))
                            .add(jsonObject.getBigDecimal("生产性生物资产")));
            jsonObject.put("流动负债", list.getJSONObject(i).getJSONArray("total_current_liab").get(0));
            jsonObject.put("非流动负债", list.getJSONObject(i).getJSONArray("total_noncurrent_liab").get(0));
            jsonObject.put("非流动负债占比", jsonObject.getFloat("非流动负债")/jsonObject.getFloat("总负债"));
            jsonObject.put("预付", list.getJSONObject(i).getJSONArray("pre_payment").get(0));
            jsonObject.put("预收", list.getJSONObject(i).getJSONArray("pre_receivable").get(0));
            jsonObject.put("长期借款", list.getJSONObject(i).getJSONArray("lt_loan").get(0));
            jsonObject.put("每股净资产", (jsonObject.getFloat("总资产")
                    - jsonObject.getFloat("总负债"))
                    / shares);
            jsonObject.put("净资产收益率", dbfx.getJSONArray("bgq").getJSONObject(i).get("净资产收益率"));
            jsonObject.put("time", list.getJSONObject(i).get("report_name"));
            jsonObject.put("流动比率", bgqzb.getJSONObject(i).get("LD"));
            jsonObject.put("速动比率", bgqzb.getJSONObject(i).get("SD"));
            jsonObject.put("产权比率", bgqzb.getJSONObject(i).get("CQBL"));
            jsonObject.put("带息负债率", bgqzb.getJSONObject(i).get("ZCFZL"));
            bgq.put(jsonObject);
        }

        String urlOfXq2 = "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol="
                + market + code
                + "&type=Q4&is_detail=true&count=3";
        Page page3 = webClient.getPage(urlOfXq2);
        WebResponse response = page3.getWebResponse();
        String jsonString2 = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString2);
        JSONArray list2 = jsonObject.getJSONObject("data").getJSONArray("list");

        for(int i = 0; i < 3; i++) {
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("总资产", list2.getJSONObject(i).getJSONArray("total_assets").get(0));
            jsonObject2.put("总负债", list2.getJSONObject(i).getJSONArray("total_liab").get(0));
            jsonObject2.put("资产负债率", jsonObject2.getFloat("总负债")/jsonObject2.getFloat("总资产"));
            jsonObject2.put("流动资产", list2.getJSONObject(i).getJSONArray("total_current_assets"));
            jsonObject2.put("非流动资产", list2.getJSONObject(i).getJSONArray("total_noncurrent_assets"));
            jsonObject2.put("商誉", list2.getJSONObject(i).getJSONArray("goodwill").get(0));
            jsonObject2.put("存货", list2.getJSONObject(i).getJSONArray("inventory").get(0));
            jsonObject2.put("应收票据及应收账款", list2.getJSONObject(i).getJSONArray("account_receivable").get(0));
            jsonObject2.put("交易性金融资产", list2.getJSONObject(i).getJSONArray("tradable_fnncl_assets").get(0));
            jsonObject2.put("固定资产", list2.getJSONObject(i).getJSONArray("fixed_asset_sum").get(0));
            jsonObject2.put("无形资产", list2.getJSONObject(i).getJSONArray("intangible_assets").get(0));
            jsonObject2.put("长期股权投资", list2.getJSONObject(i).getJSONArray("lt_equity_invest").get(0));
            jsonObject2.put("生产性生物资产", list2.getJSONObject(i).getJSONArray("productive_biological_assets").get(0));
            if(jsonObject2.get("商誉").toString() == "null") {
                jsonObject2.put("商誉", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("存货").toString() == "null") {
                jsonObject2.put("存货", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("应收票据及应收账款").toString() == "null") {
                jsonObject2.put("应收票据及应收账款", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("交易性金融资产").toString() == "null") {
                jsonObject2.put("交易性金融资产", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("固定资产").toString() == "null") {
                jsonObject2.put("固定资产", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("无形资产").toString() == "null") {
                jsonObject2.put("无形资产", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("长期股权投资").toString() == "null") {
                jsonObject2.put("长期股权投资", BigDecimal.valueOf(0));
            }
            if(jsonObject2.get("生产性生物资产").toString() == "null") {
                jsonObject2.put("生产性生物资产", BigDecimal.valueOf(0));
            }
            jsonObject2.put("8项资产合计", jsonObject2.getBigDecimal("商誉")
                    .add(jsonObject2.getBigDecimal("存货"))
                    .add(jsonObject2.getBigDecimal("应收票据及应收账款"))
                    .add(jsonObject2.getBigDecimal("交易性金融资产"))
                    .add(jsonObject2.getBigDecimal("固定资产"))
                    .add(jsonObject2.getBigDecimal("无形资产"))
                    .add(jsonObject2.getBigDecimal("长期股权投资"))
                    .add(jsonObject2.getBigDecimal("生产性生物资产")));
            jsonObject2.put("流动负债", list2.getJSONObject(i).getJSONArray("total_current_liab").get(0));
            jsonObject2.put("非流动负债", list2.getJSONObject(i).getJSONArray("total_noncurrent_liab").get(0));
            jsonObject2.put("非流动负债占比", jsonObject2.getFloat("非流动负债")/jsonObject2.getFloat("总负债"));
            jsonObject2.put("预付", list2.getJSONObject(i).getJSONArray("pre_payment").get(0));
            jsonObject2.put("预收", list2.getJSONObject(i).getJSONArray("pre_receivable").get(0));
            jsonObject2.put("长期借款", list2.getJSONObject(i).getJSONArray("lt_loan").get(0));
            jsonObject2.put("每股净资产", (jsonObject2.getFloat("总资产")
                    - jsonObject2.getFloat("总负债"))
                    / shares);
            jsonObject2.put("净资产收益率", dbfx.getJSONArray("nd").getJSONObject(i).get("净资产收益率"));
            jsonObject2.put("time", list2.getJSONObject(i).get("report_name"));
            jsonObject2.put("流动比率", ndzb.getJSONObject(i).get("LD"));
            jsonObject2.put("速动比率", ndzb.getJSONObject(i).get("SD"));
            jsonObject2.put("产权比率", ndzb.getJSONObject(i).get("CQBL"));
            jsonObject2.put("带息负债率", ndzb.getJSONObject(i).get("ZCFZL"));
            nd.put(jsonObject2);
        }
        JSONObject bigObject = new JSONObject();
        bigObject.put("年度", nd);
        bigObject.put("报告期", bgq);

        return bigObject.toString();
    }

    @Override
    public String getDbfx(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_78);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/NewFinanceAnalysis/DBFXAjaxNew?code="
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

        JSONArray bgqData = jsonObject.getJSONArray("bgq");
        JSONArray ndData = jsonObject.getJSONArray("nd");

        JSONArray bgq = new JSONArray();
        JSONArray nd = new JSONArray();

        for(int i = 0; i < 5 && i < jsonObject.getJSONArray("bgq").length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("time", jsonObject.getJSONArray("bgq").getJSONObject(i).get("REPORT_TYPE"));
            temp.put("净资产收益率", bgqData.getJSONObject(i).get("ROE"));
            temp.put("总资产净利率", bgqData.getJSONObject(i).get("JROA"));
            temp.put("权益乘数", bgqData.getJSONObject(i).get("EQUITY_MULTIPLIER"));
            temp.put("营业净利润率", bgqData.getJSONObject(i).get("SALE_NPR"));
            temp.put("总资产周转率", bgqData.getJSONObject(i).get("TOTAL_ASSETS_TR"));
            temp.put("资产负债率", bgqData.getJSONObject(i).get("DEBT_ASSET_RATIO"));
            temp.put("净利润", bgqData.getJSONObject(i).get("NETPROFIT"));
            temp.put("营业总收入", bgqData.getJSONObject(i).get("TOTAL_OPERATE_INCOME"));
            temp.put("负债总额", bgqData.getJSONObject(i).get("TOTAL_LIABILITIES"));
            temp.put("资产总额", bgqData.getJSONObject(i).get("TOTAL_ASSETS"));
            if(temp.getFloat("总资产净利率") < 2) {
                if(temp.getFloat("权益乘数") > 3) {
                    temp.put("ROA*EM组合", "低ROA*高EM");
                } else if (temp.getFloat("权益乘数") < 2) {
                    temp.put("ROA*EM组合", "低ROA*低EM");
                }
            } else if (temp.getFloat("总资产净利率") > 10) {
                if(temp.getFloat("权益乘数") > 3) {
                    temp.put("ROA*EM组合", "高ROA*高EM");
                } else if (temp.getFloat("权益乘数") < 2) {
                    temp.put("ROA*EM组合", "高ROA*低EM");
                }
            }
            bgq.put(temp);
        }
        for(int i = 0; i < 3 && i < jsonObject.getJSONArray("nd").length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("time", jsonObject.getJSONArray("nd").getJSONObject(i).get("REPORT_TYPE"));
            temp.put("净资产收益率", ndData.getJSONObject(i).get("ROE"));
            temp.put("总资产净利率", ndData.getJSONObject(i).get("JROA"));
            temp.put("权益乘数", ndData.getJSONObject(i).get("EQUITY_MULTIPLIER"));
            temp.put("营业净利润率", ndData.getJSONObject(i).get("SALE_NPR"));
            temp.put("总资产周转率", ndData.getJSONObject(i).get("TOTAL_ASSETS_TR"));
            temp.put("资产负债率", ndData.getJSONObject(i).get("DEBT_ASSET_RATIO"));
            temp.put("净利润", ndData.getJSONObject(i).get("NETPROFIT"));
            temp.put("营业总收入", ndData.getJSONObject(i).get("TOTAL_OPERATE_INCOME"));
            temp.put("负债总额", ndData.getJSONObject(i).get("TOTAL_LIABILITIES"));
            temp.put("资产总额", ndData.getJSONObject(i).get("TOTAL_ASSETS"));
            if(temp.getFloat("总资产净利率") < 2) {
                if(temp.getFloat("权益乘数") > 3) {
                    temp.put("ROA*EM组合", "低ROA*高EM");
                } else if (temp.getFloat("权益乘数") < 2) {
                    temp.put("ROA*EM组合", "低ROA*低EM");
                }
            } else if (temp.getFloat("总资产净利率") > 10) {
                if(temp.getFloat("权益乘数") > 3) {
                    temp.put("ROA*EM组合", "高ROA*高EM");
                } else if (temp.getFloat("权益乘数") < 2) {
                    temp.put("ROA*EM组合", "高ROA*低EM");
                }
            }
            if(!temp.has("ROA*EM组合")) {
                temp.put("ROA*EM组合", "--");
            }
            nd.put(temp);
        }

        JSONObject bigObject = new JSONObject().put("nd", nd).put("bgq", bgq).put("status", 0);
        return bigObject.toString();
    }

    @Override
    public String getLrb(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_78);
        Page page1 = webClient.getPage("https://xueqiu.com/people");
        String urlOfXq = "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=" + market + code
                + "&type=all&is_detail=true&count=5";
        Page page2 = webClient.getPage(urlOfXq);
        WebResponse responseOfXq = page2.getWebResponse();
        String jsonString = responseOfXq.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");

        Page page = webClient.getPage("http://emweb.securities.eastmoney.com/PC_HSF10/NewFinanceAnalysis/ZYZBAjaxNew?type=0&code="
                + market + code);
        WebResponse response = page.getWebResponse();
        String string = response.getContentAsString();
        JSONArray zyzb = new JSONObject(string).getJSONArray("data");

        if(jsonArray.length() == 0) {
            JSONObject error = new JSONObject();
            error.put("status", -1);
            error.put("message", "股票代码不合法");
            return error.toString();
        }

        JSONArray bgq = new JSONArray();
        JSONArray nd = new JSONArray();

        for(int i = 0; i < 5; i++) {
            JSONObject temp = new JSONObject();
            temp.put("基本每股收益", jsonArray.getJSONObject(i).getJSONArray("basic_eps").get(0));
            temp.put("营业总收入", jsonArray.getJSONObject(i).getJSONArray("total_revenue").get(0));
            temp.put("营业总收入同比增长率", jsonArray.getJSONObject(i).getJSONArray("total_revenue").get(1));
            temp.put("净利润", jsonArray.getJSONObject(i).getJSONArray("net_profit").get(0));
            temp.put("净利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("net_profit").get(1));
            temp.put("扣非净利润", jsonArray.getJSONObject(i).getJSONArray("net_profit_after_nrgal_atsolc").get(0));
            temp.put("扣非净利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("net_profit_after_nrgal_atsolc").get(1));
            temp.put("营业利润", jsonArray.getJSONObject(i).getJSONArray("op").get(0));
            temp.put("营业利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("op").get(1));
            temp.put("主营业务收入", jsonArray.getJSONObject(i).getJSONArray("revenue").get(0));
            temp.put("营业外收入", jsonArray.getJSONObject(i).getJSONArray("non_operating_income").get(0));
            temp.put("主营收入占比", temp.getFloat("主营业务收入")/temp.getFloat("营业总收入"));
            temp.put("投资收益", jsonArray.getJSONObject(i).getJSONArray("invest_income").get(0));
            temp.put("公允价值变动", jsonArray.getJSONObject(i).getJSONArray("income_from_chg_in_fv").get(0));
            temp.put("销售费用", jsonArray.getJSONObject(i).getJSONArray("sales_fee"));
            temp.put("管理费用", jsonArray.getJSONObject(i).getJSONArray("manage_fee"));
            temp.put("财务费用", jsonArray.getJSONObject(i).getJSONArray("financing_expenses"));
            temp.put("毛利率", zyzb.getJSONObject(i).get("XSMLL"));
            temp.put("净利率", zyzb.getJSONObject(i).get("XSJLL"));
            temp.put("研发投入占营业收入比例",
                    jsonArray.getJSONObject(i).getJSONArray("rad_cost").getFloat(0)
                            / temp.getFloat("营业总收入"));
            bgq.put(temp);
        }

        String urlOfXq2 = "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=" + market + code
                + "&type=Q4&is_detail=true&count=3";
        Page page3 = webClient.getPage(urlOfXq2);
        WebResponse responseOfXq2 = page3.getWebResponse();
        String jsonString2 = responseOfXq2.getContentAsString();
        JSONObject jsonObject2 = new JSONObject(jsonString2);
        jsonArray = jsonObject2.getJSONObject("data").getJSONArray("list");

        for(int i = 0; i < 3; i++) {
            JSONObject temp = new JSONObject();
            temp.put("基本每股收益", jsonArray.getJSONObject(i).getJSONArray("basic_eps").get(0));
            temp.put("营业总收入", jsonArray.getJSONObject(i).getJSONArray("total_revenue").get(0));
            temp.put("营业总收入同比增长率", jsonArray.getJSONObject(i).getJSONArray("total_revenue").get(1));
            temp.put("净利润", jsonArray.getJSONObject(i).getJSONArray("net_profit").get(0));
            temp.put("净利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("net_profit").get(1));
            temp.put("扣非净利润", jsonArray.getJSONObject(i).getJSONArray("net_profit_after_nrgal_atsolc").get(0));
            temp.put("扣非净利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("net_profit_after_nrgal_atsolc").get(1));
            temp.put("营业利润", jsonArray.getJSONObject(i).getJSONArray("op").get(0));
            temp.put("营业利润同比增长率", jsonArray.getJSONObject(i).getJSONArray("op").get(1));
            temp.put("主营业务收入", jsonArray.getJSONObject(i).getJSONArray("revenue").get(0));
            temp.put("营业外收入", jsonArray.getJSONObject(i).getJSONArray("non_operating_income").get(0));
            temp.put("主营收入占比", temp.getFloat("主营业务收入")/temp.getFloat("营业总收入"));
            temp.put("投资收益", jsonArray.getJSONObject(i).getJSONArray("invest_income").get(0));
            temp.put("公允价值变动", jsonArray.getJSONObject(i).getJSONArray("income_from_chg_in_fv").get(0));
            temp.put("销售费用", jsonArray.getJSONObject(i).getJSONArray("sales_fee"));
            temp.put("管理费用", jsonArray.getJSONObject(i).getJSONArray("manage_fee"));
            temp.put("财务费用", jsonArray.getJSONObject(i).getJSONArray("financing_expenses"));
            temp.put("毛利率", zyzb.getJSONObject(4*i).get("XSMLL"));
            temp.put("净利率", zyzb.getJSONObject(4*i).get("XSJLL"));
            temp.put("研发投入占营业收入比例",
                    jsonArray.getJSONObject(i).getJSONArray("rad_cost").getFloat(0)
                            / temp.getFloat("营业总收入"));
            nd.put(temp);
        }

        JSONObject bigObject = new JSONObject();
        bigObject.put("bgq", bgq);
        bigObject.put("nd", nd);

        return bigObject.toString();
    }

    @Override
    public String getXjllb(String market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_78);
        Page page1 = webClient.getPage("https://xueqiu.com/people");
        String urlOfXq = "https://stock.xueqiu.com/v5/stock/finance/cn/cash_flow.json?symbol=" + market + code
                + "&type=all&is_detail=true&count=5";
        Page page2 = webClient.getPage(urlOfXq);
        WebResponse responseOfXq = page2.getWebResponse();
        String jsonString = responseOfXq.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");

        if(jsonArray.length() == 0) {
            JSONObject error = new JSONObject();
            error.put("status", -1);
            error.put("message", "股票代码不合法");
            return error.toString();
        }

        Double shares = Double.parseDouble(getGb(market, code));
        JSONArray bgq = new JSONArray();
        JSONArray nd = new JSONArray();

        for(int i = 0; i < 5; i++) {
            JSONObject temp = new JSONObject();
            temp.put("经营现金流", jsonArray.getJSONObject(i).getJSONArray("ncf_from_oa").get(0));
            temp.put("投资现金流", jsonArray.getJSONObject(i).getJSONArray("ncf_from_ia").get(0));
            temp.put("筹资现金流", jsonArray.getJSONObject(i).getJSONArray("ncf_from_fa").get(0));
        }

        return null;
    }

    private String getGb(String market, String code) throws IOException {
        JSONObject bigObject = new JSONObject();
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://emweb.securities.eastmoney.com/PC_HSF10/CapitalStockStructure/PageAjax?code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        return jsonObject.getJSONArray("gbjg").getJSONObject(0).get("TOTAL_SHARES").toString();
    }

    private JSONArray getZyzb(String market, String code, int type) throws IOException {
        JSONObject bigObject = new JSONObject();
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "emweb.securities.eastmoney.com/PC_HSF10/NewFinanceAnalysis/ZYZBAjaxNew?type="
                + type +"&code=" + market + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < 5-2*type; i++) {
            jsonArray.put(jsonObject.getJSONArray("data").getJSONObject(i));
        }
        return jsonArray;
    }
}
