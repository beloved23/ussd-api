package com.ft.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class EventDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ZonedDateTime requestAt;

	private ZonedDateTime responseAt;

	private String from;

	private String to;

	private String text;
	
	private String channel;
	
	private int ussdOp;
	
	private String sessionId;
	
	private boolean error = false;

	@Override
	public String toString() {
		return "EventDTO [requestAt=" + requestAt + ", responseAt=" + responseAt + ", from=" + from + ", to=" + to
				+ ", text=" + text + "]";
	}

	public ZonedDateTime getRequestAt() {
		return requestAt;
	}

	public void setRequestAt(ZonedDateTime requestAt) {
		this.requestAt = requestAt;
	}

	public ZonedDateTime getResponseAt() {
		return responseAt;
	}

	public void setResponseAt(ZonedDateTime responseAt) {
		this.responseAt = responseAt;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public EventDTO text(String text) {
		this.text = text;
		return this;
	}

	public EventDTO requestAt(ZonedDateTime requestAt) {
		this.requestAt = requestAt;
		return this;
	}

	public EventDTO responseAt(ZonedDateTime responseAt) {
		this.responseAt = responseAt;
		return this;
	}

	public EventDTO from(String from) {
		this.from = from;
		return this;
	}

	public EventDTO to(String to) {
		this.to = to;
		return this;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public EventDTO channel(String channel) {
		this.channel = channel;
		return this;
	}

	public int getUssdOp() {
		return ussdOp;
	}

	public void setUssdOp(int ussdOp) {
		this.ussdOp = ussdOp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
	
	
}
