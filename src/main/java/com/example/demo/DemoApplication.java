package com.example.demo;

import com.example.demo.service.MessageProcessorService;
import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class
})
public class DemoApplication {

	public static void main(String[] args)   {
		ApplicationInsights.attach();
		ApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
		String sessionId = UUID.randomUUID().toString();

		MessageProcessorService messageProcessorService = applicationContext.getBean(MessageProcessorService.class);
		messageProcessorService.registerSubscription(sessionId);
	}
}
