package com.fly.springbootinit.service.impl;

import com.fly.springbootinit.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartServiceImplTest {
    @Resource
    private ChartService chartService;

    @Test
    void name() {
        boolean b = chartService.ChangeLongTimeWaitToFailed();
        assertTrue(b);
    }
}