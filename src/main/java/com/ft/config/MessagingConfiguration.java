package com.ft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.ft.service.dto.EventDTO;

@Configuration
public class MessagingConfiguration {

	private final Logger log = LoggerFactory.getLogger(MessagingConfiguration.class);
	
	@Autowired
	HazelcastInstance hazelcastInstance;
	
	/**
	 * Event Queue
	 * @return
	 */
	@Bean
	public IQueue<EventDTO> eventQueue() {
		IQueue<EventDTO> result = hazelcastInstance.getQueue(Constants.EVENT_TOPIC);
		return result;
	}
}
