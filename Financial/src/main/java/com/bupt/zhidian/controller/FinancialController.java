package com.bupt.zhidian.controller;

import com.bupt.zhidian.service.FinancialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Api(tags = "自选股票")
@RestController
@RequestMapping(value = "/select", method = RequestMethod.POST)
public class FinancialController {
    @Autowired
    FinancialService financialService;

    @ApiOperation("财务分析/杜邦分析 SH 600000")
    @RequestMapping(value = "financial/1")
    public String getDbfx(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return financialService.getDbfx(market, code);
    }

    @ApiOperation("财务分析/资产负债表 SH 600000")
    @RequestMapping(value = "financial/2")
    public String getZcfzb(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return financialService.getZcfzb(market, code);
    }

    @ApiOperation("财务分析/利润表 SH 600000")
    @RequestMapping(value = "financial/3")
    public String getLrb(@RequestParam("market")String market, @RequestParam("code")String code) throws IOException {
        return financialService.getLrb(market, code);
    }
}
