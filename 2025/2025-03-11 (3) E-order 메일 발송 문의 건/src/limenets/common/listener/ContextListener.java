package com.limenets.common.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.limenets.common.util.Converter;

public class ContextListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
	    String springProfileActive = "dev";
		try{
			String profileName = Converter.toStr(System.getProperty("site.profile"));
			if (!StringUtils.equals("", profileName)) {
				springProfileActive = profileName;
			}
			
		} catch(Exception e) {
		} finally {
		    System.setProperty("spring.profiles.active", springProfileActive);
		}
		
		if(logger.isInfoEnabled()){
			logger.info("Application active profile : " + System.getProperty("spring.profiles.active"));
		}
		System.out.println("================================");
		System.out.println(System.getProperties());
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(logger.isInfoEnabled()){
			logger.info("Application shutdown.");
		}		
	}
}
