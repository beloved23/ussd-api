package com.ft.web.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class LoggerResource {

	private final Logger log = LoggerFactory.getLogger(LoggerResource.class);
	
	@GetMapping("/logger")
	public ResponseEntity<String> logRequest(@RequestParam(required=false) Map<String, String> request) {
		log.info("Got a request: {}", request);
		return ResponseEntity.ok("OK");
	}
}
