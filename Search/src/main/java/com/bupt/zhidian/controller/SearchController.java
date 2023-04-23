package com.bupt.zhidian.controller;

import com.bupt.zhidian.dao.SearchDao;
import com.bupt.zhidian.entity.Stock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandle;
import java.util.List;

@Api(tags = "股票搜索")
@RestController
@RequestMapping(value = "/search")
public class SearchController {
    @Autowired
    SearchDao searchDao;

    @ApiOperation("搜索")
    @PostMapping()
    public String search(@RequestParam("content") String content) {
        List<Stock> list1 = searchDao.findByCode(content);
        List<Stock> list2 = searchDao.findByName(content);
        list1.addAll(list2);
        JSONObject bigObject = new JSONObject().put("data", list1);
        return bigObject.toString();
    }
}
