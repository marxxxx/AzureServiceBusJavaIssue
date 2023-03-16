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
    private static final int MAX_NUMBER_OF_SUBSCRIPTIONS_PER_RECEIVER = 255;
    private static Map<String, ServiceBusSessionReceiverAsyncClient> sessionServiceBusSessionReceiverAsyncClientMap = new HashMap<>();
    private static Map<ServiceBusSessionReceiverAsyncClient, Integer> serviceBusSessionReceiverAsyncClientCounterMap = new HashMap<>();

    public Mono<ServiceBusReceiverAsyncClient> getServiceBusReceiverClient(String sessionIndex) {
        ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverClient = sessionServiceBusSessionReceiverAsyncClientMap.get(sessionIndex);
        if (serviceBusSessionReceiverClient == null){
            serviceBusSessionReceiverClient = getServiceBusReceiverClient();
            sessionServiceBusSessionReceiverAsyncClientMap.put(sessionIndex, serviceBusSessionReceiverClient);
            Integer activeConnections = serviceBusSessionReceiverAsyncClientCounterMap.getOrDefault(serviceBusSessionReceiverClient, 0);
            serviceBusSessionReceiverAsyncClientCounterMap.put(serviceBusSessionReceiverClient, activeConnections+1);
        }
        try{
            return serviceBusSessionReceiverClient.acceptSession(sessionIndex);
        }catch (Exception e){
            log.error("Error accepting session on serviceBusSessionReceiverClient for {} ", sessionIndex);
            return Mono.empty();
        }
    }

    private ServiceBusSessionReceiverAsyncClient getNewServiceBusSessionReceiverAsyncClient(){
        return new ServiceBusClientBuilder()
                .connectionString(connectionStringQueueListener)
                .sessionReceiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .maxAutoLockRenewDuration(Duration.ofMinutes(5))
                .queueName(queueName)
                .buildAsyncClient();
    }

    private ServiceBusSessionReceiverAsyncClient getServiceBusReceiverClient() {
        ServiceBusSessionReceiverAsyncClient currentClient = serviceBusSessionReceiverAsyncClientCounterMap.entrySet().stream().filter(x -> x.getValue() < MAX_NUMBER_OF_SUBSCRIPTIONS_PER_RECEIVER).map(Map.Entry::getKey).findFirst().orElse(null);
        if (currentClient == null){
            currentClient = getNewServiceBusSessionReceiverAsyncClient();
            log.info("Created async azure service bus session receiver client. [clients={}, connectionString={}, queue={}]",
                    sessionServiceBusSessionReceiverAsyncClientMap.size(), connectionStringQueueListener, queueName);
        }
        return  currentClient;
    }
}
