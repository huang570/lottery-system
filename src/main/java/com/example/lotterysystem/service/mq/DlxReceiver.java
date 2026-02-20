package com.example.lotterysystem.service.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.*;

/**
 * 处理死信队列
 */
@Component
@RabbitListener(queues = DLX_QUEUE_NAME)
public class DlxReceiver {

    private static final Logger logger = LoggerFactory.getLogger(DlxReceiver.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void process(Map<String, String> message) {
        // 死信队列处理方法

        logger.info("开始处理异常消息，死信队列");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING,message);


    }
}
