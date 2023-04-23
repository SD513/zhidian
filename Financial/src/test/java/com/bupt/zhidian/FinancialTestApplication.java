package com.bupt.zhidian;

import com.bupt.zhidian.service.FinancialService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FinancialTestApplication {
    @Autowired
    FinancialService financialService;
    @Test
    public void fuck() throws IOException, ParseException {
        System.out.println(financialService.getZcfzb("SZ", "300001"));
    }
}
