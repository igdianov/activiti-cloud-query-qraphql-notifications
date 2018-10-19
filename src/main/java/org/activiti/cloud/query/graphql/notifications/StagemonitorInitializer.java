package org.activiti.cloud.query.graphql.notifications;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.stagemonitor.web.servlet.initializer.ServletContainerInitializerUtil;

@Configuration
public class StagemonitorInitializer implements ServletContextInitializer {
	
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // necessary for spring boot 2.0.0 until stagemonitor supports it natively
        ServletContainerInitializerUtil.registerStagemonitorServletContainerInitializers(servletContext);
    }
}    