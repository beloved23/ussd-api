package com.ft.service;

import java.time.ZonedDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ft.config.ApplicationProperties;
import com.ft.config.Constants;
import com.ft.domain.Notification;
import com.ft.service.dto.EventDTO;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

/**
 *
 */
@Service
public class EventService {

	private final Logger log = LoggerFactory.getLogger(EventService.class);

    @Autowired
    ApplicationProperties props;

    @Autowired
    ApplicationContext appContext;
    
    @Autowired
    private IQueue<EventDTO> eventQueue;
    
    @Autowired
    HazelcastInstance hazelcastInstance;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Async
    public void messageReceivedWithLog(
            String channel,
            String from,
            String to,
            String text,
            Map<String, String> params
        ) {
    	EventDTO event = new EventDTO()
        		.from(from)
        		.to(to)
        		.text(text)
        		.requestAt(ZonedDateTime.now())
        		;
    	try {
	    	ResponseEntity<String> response = onMessageReceived(channel, from, to, text);
	    	event.channel(channel).setText(response.getBody());
    	} catch (Exception e) {
    		event.channel(channel).setText("Xin loi, hien tai he thong dang ban. Xin quy khach vui long thu lai sau.");
    	}
    	eventQueue.offer(event.channel(Constants.CHANNEL_SMS));
    	
    }

    /**
     * Fire an event when message received from one channel
     *
     * @param channel The channel message come in, can be WEB, WAP, SMS, USSD
     * @param from    Normally the MSISDN number that trigger the request
     * @param to      Normally the short code that receive the request
     * @param text    The message body
     * @return 202 Accepted for Processing 403 Forbidden 500 Internal Error
     */
    public ResponseEntity<String> onMessageReceived(
        String channel,
        String from,
        String to,
        String text
    ) {
    	from = props.msisdn(from);
    	text = text.toUpperCase().trim();
    	log.info(">> [{}] {} -> {} | {}", channel, from, to, text);
        Notification notification = new Notification()
        		.msisdn(from)
        		.requestPayload(text)
        		.requestAt(ZonedDateTime.now())
//        		.channel(channel)
        		.productId(to);
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
        
    }
}
