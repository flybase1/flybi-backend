package com.fly.springbootinit.bizmq;

import com.fly.springbootinit.api.YuApi;
import com.fly.springbootinit.bizmq.Constant.BIMQConstant;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.model.entity.Chart;
import com.fly.springbootinit.model.enums.ChartStatusEnum;
import com.fly.springbootinit.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BIMessageConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private YuApi yuApi;

    //指定程序监听的消息队列确认机制

    /**
     * 传递的是chart的id
     *
     * @param message  传递消息
     * @param channel  交换机
     * @param deliverTag
     */
    @SneakyThrows
    @RabbitListener( queues = {BIMQConstant.BI_QUEUE_NAME}, ackMode = "MANUAL" )
    public void receiveMessage(String message, Channel channel, @Header( AmqpHeaders.DELIVERY_TAG ) long deliverTag) {
        if (message == null) {
            channel.basicNack(deliverTag, false, false);
            //死信
            channel.basicPublish("", BIMQConstant.BI_DLX_DIRECT_ROUTING_KEY_NAME, null, message.getBytes());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if (chart == null) {
            channel.basicNack(deliverTag, false, false);
            channel.basicPublish("", BIMQConstant.BI_DLX_DIRECT_ROUTING_KEY_NAME, null, message.getBytes());
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "数据为空");
        }

        //先修改图表状态为'执行中'，执行完毕后修改为'已完成'，保存执行结果，执行失败后，状态修改为'失败'记录失败信息
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(ChartStatusEnum.Running.getValue());
        boolean success = chartService.updateById(updateChart);
        if (!success) {
            // todo 进一步完善失败,死信机制
            channel.basicNack(deliverTag, false, false);
            channel.basicPublish("", BIMQConstant.BI_DLX_DIRECT_ROUTING_KEY_NAME, null, message.getBytes());
            updateStatusLoadingToFailure(chartId);
            handleChartUpdateError(chart.getId(), "更新图表状态失败");
            return;
        }

        // 调用AI
        String chartResult = yuApi.doChart(CommonConstant.BI_MODEL_ID, builderUserInput(chart));
        String[] splits = chartResult.split("【【【【【");

        if (splits.length > 3) {
            handleChartUpdateError(chart.getId(), "AI生成错误");
            // throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            return;
        }
        String genChart = splits[1].trim();

        //todo 去除无关的内容
        int startIndex = genChart.indexOf("{");
        int lastIndex = genChart.lastIndexOf("}");
        if (startIndex != -1 && lastIndex != -1 && lastIndex > startIndex) {
            String extractedContent = genChart.substring(startIndex, lastIndex + 1).trim();
            System.out.println(extractedContent);
        } else {
            System.out.println("未找到匹配的内容");
        }

        String genResult = splits[2].trim();
        Chart updateChartSuccess = new Chart();
        updateChartSuccess.setId(chart.getId());
        updateChartSuccess.setStatus(ChartStatusEnum.Succeed.getValue());
        updateChartSuccess.setGenChart(genChart);
        updateChartSuccess.setGenResult(genResult);
        boolean b = chartService.updateById(updateChartSuccess);
        if (!b) {
            channel.basicNack(deliverTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表状态失败");
        }

        log.info("receive message = {}", message);
        // 消息确认
        channel.basicAck(deliverTag, false);

    }

    /**
     * 处理错误
     * @param chartId
     * @param execMessage
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartSuccess = new Chart();
        updateChartSuccess.setId(chartId);
        updateChartSuccess.setStatus(ChartStatusEnum.Failed.getValue());
        updateChartSuccess.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartSuccess);
        if (!b) {
            log.error(chartId + ": 更新失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图表更新失败");
        }
    }

    /**
     * 处理输入
     * @param chart
     * @return
     */
    private String builderUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");

        // 文件处理,压缩后的数据
        stringBuilder.append(chartData).append("\n");
        return stringBuilder.toString();
    }

    /**
     * 当消息进入死信队列后，此时将状态改为failed
     *
     * @param chartId
     * @return
     */
    public void updateStatusLoadingToFailure(Long chartId) {
        Chart chart = chartService.getById(chartId);
        if (chart != null) {
            chart.setStatus(ChartStatusEnum.Failed.getValue());
            boolean success = chartService.updateById(chart);
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
