package com.fly.springbootinit.AIChat;

import com.fly.springbootinit.api.YuApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

@SpringBootTest
public class AIChat {

    @Resource
    private YuApi yuApi;

    @Test
    void test() {
        String message = yuApi.doChart(1677993065421332481L, "你是谁");
        System.out.println(message);
    }
}
