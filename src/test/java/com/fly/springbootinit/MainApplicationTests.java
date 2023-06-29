package com.fly.springbootinit;

import com.fly.springbootinit.config.WxOpenConfig;

import javax.annotation.Resource;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private YuCongMingClient client;

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }


    @Test
    void name() {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1651468516836098050L);
        devChatRequest.setMessage("fly");

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        System.out.println(response.getData());
    }




}
