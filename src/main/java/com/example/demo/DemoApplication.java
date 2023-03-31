package com.example.demo;

import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.example.demo.factory.ServiceBusReceiverFactory;
import com.example.demo.service.MessageProcessorService;
import com.microsoft.applicationinsights.attach.ApplicationInsights;
import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import reactor.core.Disposable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class
})
public class DemoApplication {

	static Disposable subscription;
	static ServiceBusSessionReceiverAsyncClient client;


	public static void main(String[] args)   {
		ApplicationInsights.attach();
		ApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
		String sessionId = UUID.randomUUID().toString();


		MessageProcessorService messageProcessorService = applicationContext.getBean(MessageProcessorService.class);
		ServiceBusReceiverFactory factory = applicationContext.getBean(ServiceBusReceiverFactory.class);

		client = factory.getNewServiceBusSessionReceiverAsyncClient();

		subscription = messageProcessorService.registerSubscription(client, sessionId);

		final int refreshInterval = 60000 * 15;
		new Timer().scheduleAtFixedRate(new TimerTask() {

			private int refreshCount = 0;
			@Override
			public void run() {
				System.out.println("***** Refreshing ******.");

				subscription.dispose();
				client.close();

				client = factory.getNewServiceBusSessionReceiverAsyncClient();

				subscription = messageProcessorService.registerSubscription(client, sessionId);

				refreshCount++;

				System.out.println("***** Successfully refreshed " + refreshCount + " *****");
			}
		}, refreshInterval, refreshInterval);
	}
}
