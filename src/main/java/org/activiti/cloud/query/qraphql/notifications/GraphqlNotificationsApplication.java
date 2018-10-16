package org.activiti.cloud.query.qraphql.notifications;

import org.activiti.cloud.services.query.notifications.config.EnableActivitiNotificationsGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableActivitiNotificationsGateway
public class GraphqlNotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlNotificationsApplication.class, args);
	}
}
