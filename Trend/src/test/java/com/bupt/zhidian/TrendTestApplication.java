package com.bupt.zhidian;

import com.bupt.zhidian.service.TrendService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrendTestApplication {
    @Autowired
    TrendService trendService;
    @Test
    public void contextLoads() throws Exception {
        System.out.println(trendService.getPageOfTrend1(1, "600000"));
    }
}
