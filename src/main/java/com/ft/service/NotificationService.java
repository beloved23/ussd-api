package com.ft.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ft.config.ApplicationProperties;
import com.ft.config.Constants;
import com.ft.config.SmscSmppConfiguration;
import com.ft.service.dto.EventDTO;
import com.hazelcast.core.IQueue;

/**
 * Consume notification messages from SS7 channel and send out via MtFwd
 * @author dinhtrung
 *
 */
@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	
	@Autowired
	ApplicationProperties props;
	
	@Autowired
	SmscSmppConfiguration smscSmppConfiguration;
	
	@Autowired
    private IQueue<EventDTO> eventQueue;
	
	/**
	 * Check queue for pending messages
	 */
	@Scheduled(fixedDelay = 1000L)
	public void processPendingResponses() {
		log.debug("Processing enqueued response");
		EventDTO evt = eventQueue.poll();
		if (evt == null) return;
		
		log.debug("Got event: {}", evt);
		
		if (evt.getChannel().equalsIgnoreCase(Constants.CHANNEL_SMS)) {
			try {
				smscSmppConfiguration.sendAsyncMessage(evt.getTo(), evt.getText(), evt.getFrom());
			} catch (Exception e) {
				log.error("Cannot send SMS back to client", e);
			}
		}
		
	}
	
}
