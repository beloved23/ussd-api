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

	private String source;
	
	private byte dataCoding;
	
	private byte sourceTon;
	
	private byte sourceNpi;

	private String dest;
	
	private byte destTon;
	
	private byte destNpi;

	private String text;
	
	private String channel;
	
	private String ussdOp;
	
	private String sessionId;
	
	private Integer sessionNumber;
	
	private Integer sequenceNumber;
	
	private Boolean sessionActive;
	
	private boolean error = false;

	@Override
	public String toString() {
		return "EventDTO [requestAt=" + requestAt + ", responseAt=" + responseAt + ", from=" + source + ", to=" + dest
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public byte getSourceTon() {
		return sourceTon;
	}

	public void setSourceTon(byte sourceTon) {
		this.sourceTon = sourceTon;
	}

	public byte getSourceNpi() {
		return sourceNpi;
	}

	public void setSourceNpi(byte sourceNpi) {
		this.sourceNpi = sourceNpi;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public byte getDestTon() {
		return destTon;
	}

	public void setDestTon(byte destTon) {
		this.destTon = destTon;
	}

	public byte getDestNpi() {
		return destNpi;
	}

	public void setDestNpi(byte destNpi) {
		this.destNpi = destNpi;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUssdOp() {
		return ussdOp;
	}

	public void setUssdOp(String ussdOp) {
		this.ussdOp = ussdOp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getSessionNumber() {
		return sessionNumber;
	}

	public void setSessionNumber(Integer sessionNumber) {
		this.sessionNumber = sessionNumber;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Boolean getSessionActive() {
		return sessionActive;
	}

	public void setSessionActive(Boolean sessionActive) {
		this.sessionActive = sessionActive;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public byte getDataCoding() {
		return dataCoding;
	}

	public void setDataCoding(byte dataCoding) {
		this.dataCoding = dataCoding;
	}


	
}
