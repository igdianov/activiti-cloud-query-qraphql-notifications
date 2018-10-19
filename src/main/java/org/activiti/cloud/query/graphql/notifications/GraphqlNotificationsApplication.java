package org.activiti.cloud.query.graphql.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.stagemonitor.core.Stagemonitor;

@SpringBootApplication
public class GraphqlNotificationsApplication {

	public static void main(String[] args) {
        Stagemonitor.init();
		
		SpringApplication.run(GraphqlNotificationsApplication.class, args);
	}
}
