package com.ft.service;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    @Autowired
    ObjectMapper objectMapper;
    
    @PostConstruct
    public void testEndpoint() {
    	try {
    		ResponseEntity<String> response = restTemplate.getForEntity(props.getCallbackUrl(), String.class);
    		log.info("API Endpoint Response: {} | {}", response.getStatusCodeValue(), response.getBody());
    	} catch (Exception e) {
    		log.error("Endpoint not ready yet: {}", e);
    	}
    }
    
    @Async
    public void messageReceivedWithLog(
            String smscId,
            EventDTO mo,
            Map<String, String> params
        ) throws JsonMappingException, JsonProcessingException {
    	EventDTO responseEvent = objectMapper.readValue(objectMapper.writeValueAsString(mo), EventDTO.class);
    	responseEvent.setSource(mo.getDest()); responseEvent.setSourceTon(mo.getDestTon()); responseEvent.setSourceNpi(mo.getDestNpi());
    	responseEvent.setDest(mo.getSource()); responseEvent.setDestTon(mo.getSourceTon()); responseEvent.setDestNpi(mo.getSourceNpi());
    	responseEvent.setSessionId(smscId);
    	responseEvent.setUssdOp(props.getMoEnd()); // default information
    	params.put("msisdn", mo.getSource());
    	params.put("code", mo.getDest());
    	params.put("INPUT", mo.getText());
    	params.putAll(props.getQueryParams());
    	try {
    		ResponseEntity<String> response = restTemplate.getForEntity(props.getCallbackUrl(), String.class, params);
	    	responseEvent.setText(response.getBody());
	    	if (response.getStatusCodeValue() == 200) {
	    		responseEvent.setUssdOp(props.getMoContinue()); // CONTINUE
	    		responseEvent.setError(false);
	    		responseEvent.setSessionActive(true);
	    	} else {
	    		responseEvent.setUssdOp(props.getMoEnd()); // END
	    		responseEvent.setSessionActive(false);
	    	}
    	} catch (HttpStatusCodeException e) {
    		log.error("Error from http client: {} | {}", e.getStatusCode(), e.getResponseBodyAsString());
    		if (e.getStatusCode().is4xxClientError()) {
    			responseEvent.setUssdOp(props.getMoEnd());
    		} else {
    			responseEvent.setUssdOp(props.getMoErr());
    		}
    		responseEvent.setError(true);
    	} catch (Exception e) {
    		responseEvent.setError(true);
    		responseEvent.setText(props.getErrorText());
    	}
    	eventQueue.offer(responseEvent);
    	
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
