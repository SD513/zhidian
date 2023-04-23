package com.bupt.zhidian.controller;

import com.bupt.zhidian.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@Api(tags = "自选股票")
@RestController
@RequestMapping(value = "/select", method = RequestMethod.POST)
public class AnalysisController {
    @Autowired
    AnalysisService analysisService;

    @ApiOperation("基本面分析/公司概况/公司简介 sh 600000")
    @RequestMapping(value = "general/1")
    public String getCompanyIntroduce(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getCompanyCS(market, code);
    }

    @ApiOperation("基本面分析/公司概况/所属板块 SH 600000")
    @RequestMapping(value = "general/2")
    public String getSsbk(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getSsbk(market, code);
    }


    @ApiOperation("基本面分析/公司概况/主营构成 sh 600000")
    @RequestMapping(value = "general/3")
    public String getComposition(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getComposition(market, code);
    }

    @ApiOperation("基本面分析/公司概况/业绩趋势 sh 600000")
    @RequestMapping(value = "general/4")
    public String getAchievement(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getAchievement(market, code);
    }

    @ApiOperation("基本面分析/公司概况/股东股本 SH 600000")
    @RequestMapping(value = "general/7")
    public String getGdgb(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getGdgb(market, code);
    }

    @ApiOperation("基本面分析/公司概况/机构持仓 SH 600000")
    @RequestMapping(value = "general/8")
    public String getJgcc(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException, ParseException {
        return analysisService.getJgcc(market, code);
    }

    @ApiOperation("基本面分析/公司概况/十大流通股东变动 SH 600000")
    @RequestMapping(value = "general/9")
    public String getSdltgd(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getSdltgdbd(market, code);
    }

    @ApiOperation("基本面分析/公司概况/基金持仓 SH 600000")
    @RequestMapping(value = "general/10")
    public String getJjcg(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException, ParseException {
        return analysisService.getQsccjj(market, code);
    }

    @ApiOperation("基本面分析/公司概况/公司高管 SH 600000")
    @RequestMapping(value = "general/11")
    public String getCEO(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getCEO(market, code);
    }

    @ApiOperation("基本面分析/公司概况/分红送配 SH 600000")
    @RequestMapping(value = "general/12")
    public String getFhsp(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getFhsp(market, code);
    }

    @ApiOperation("基本面分析/公司概况/主要指标 SH 600000")
    @RequestMapping(value = "general/13")
    public String getZyzb(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return analysisService.getZyzb(market, code);
    }
}
