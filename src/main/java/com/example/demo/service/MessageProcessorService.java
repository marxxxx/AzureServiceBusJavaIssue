package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.example.demo.factory.ServiceBusReceiverFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class MessageProcessorService {
    private final ServiceBusReceiverFactory serviceBusReceiverFactory;

    public Disposable registerSubscription(ServiceBusSessionReceiverAsyncClient client, String sessionId) {
        Mono<ServiceBusReceiverAsyncClient> monoReceiver = client.acceptSession(sessionId);
        log.info("Registering subscription for current session id: {}", sessionId);

        Disposable subscription = Flux.usingWhen(
                        monoReceiver,
                        this::receiveMessages,
                        receiver -> closeReceiver(receiver, sessionId, "completed"),
                        (resource, error) -> {
                            log.error("Exception in received ServiceBusReceivedMessage. Message: [{}], ServiceBusAsyncClientSessionId:[{}]",
                                    error.getMessage(), resource.getSessionId(), error);

                            return closeReceiver(resource, sessionId, "Error"); //build new subscribtion ??
                        },
                        receiver -> closeReceiver(receiver, sessionId, "canceled")
                )
                .subscribe(
                        this::processMessage,
                        error -> log.error("Could not receive message over service bus. [session: {}, error: {}]", sessionId, error)
                );

        return subscription;
    }

    private Publisher<?> closeReceiver(ServiceBusReceiverAsyncClient receiver, String sessionId, String reason) {
        return Mono.fromRunnable(() -> {
            log.warn("Session receiver will be closed.[reason={}] [session={}]",reason, sessionId);
            receiver.close();
        });
    }

    public Flux<ServiceBusReceivedMessage> receiveMessages(ServiceBusReceiverAsyncClient receiver) {
        try {
            return receiver.receiveMessages().onErrorContinue((a, t) -> log.error("Exception in original Message: [{}] [sessionId={}]", a.getMessage(), receiver.getSessionId(), a));
        } catch (Exception e){
            log.error("Exception receiving message via ServiceBusReceiverAsyncClient [sessionId={}]", receiver.getSessionId(), e);
            return Flux.empty();
        }
    }

    static int ii = 0;

    private void processMessage(ServiceBusReceivedMessage message) {
        String body = new String(message.getBody().toBytes(), StandardCharsets.UTF_8);
        log.info("Message with id: {} has been processed. \n Session: {}, Body: {}", message.getMessageId(), message.getSessionId(), body);
    }
}
