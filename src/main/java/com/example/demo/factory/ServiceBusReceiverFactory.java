package com.example.demo.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Setter
@Getter
public class  ServiceBusReceiverFactory {
    @Value("${messaging.connectionStringQueueListener}")
    private String connectionStringQueueListener;

    @Value("${messaging.queueName}")
    private String queueName;

    public Mono<ServiceBusReceiverAsyncClient> getServiceBusReceiverClient(String sessionIndex) {

            ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverClient = getNewServiceBusSessionReceiverAsyncClient();
            return serviceBusSessionReceiverClient.acceptSession(sessionIndex);
    }

    public ServiceBusSessionReceiverAsyncClient getNewServiceBusSessionReceiverAsyncClient(){
        return new ServiceBusClientBuilder()
                .connectionString(connectionStringQueueListener)
                .sessionReceiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .maxAutoLockRenewDuration(Duration.ofMinutes(5))
                .queueName(queueName)
                .buildAsyncClient();
    }
}
