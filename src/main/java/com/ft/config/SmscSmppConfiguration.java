package com.ft.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.gsm.GsmUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.ft.service.dto.EventDTO;
import com.ft.smpp.OutboundClient;
import com.ft.smpp.ReconnectionDaemon;
import com.ft.smpp.impl.UssdClientMessagingService;

@ConfigurationProperties(prefix = "smpp-connector", ignoreUnknownFields = false)
@ConditionalOnProperty(prefix = "smpp-connector", name = { "conn" })
public class SmscSmppConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SmscSmppConfiguration.class);
	
	public static final Random random = new Random();

	private List<SmppSessionConfiguration> conn;
	
	public static Map<String, OutboundClient> activeSessions = new HashMap<String, OutboundClient>();
	
	@Autowired
	private UssdClientMessagingService smppClientMessageService;

	@PostConstruct
	protected void initialize() {
		initializeSmppConn();
		random.setSeed(System.currentTimeMillis());
		log.debug("SMPP Connector: {}", this.toString());
	}
	

	private void initializeSmppConn() {
		if (conn == null) return;
		log.info("== Trying to connect to our SMSC for sending SMS ==");
		for (SmppSessionConfiguration cfg : conn) {
			OutboundClient conn = new OutboundClient();
			conn.initialize(cfg, smppClientMessageService);
			conn.scheduleReconnect();
			activeSessions.put(conn.getSessionId(), conn);
			log.info("Add new SMSC session: {} -- {}:{}", conn.getSessionId(), conn.getConfiguration().getHost(),
					conn.getConfiguration().getPort());
		}
	}


	/**
	 * Submit one SMS via SMSC
	 * 
	 * @param source
	 * @param text
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public void sendAsyncMessage(EventDTO event) throws Exception {
		log.info("SubmitSm: {} -> {} : {}", event.getSource(), event.getDest(), event.getText());
		SmppSession session = activeSessions.get(event.getSessionId()).getSession();
		if ((session != null) && session.isBound()) {
			byte[][] segments = GsmUtil.createConcatenatedBinaryShortMessages(
					CharsetUtil.encode(event.getText(), CharsetUtil.CHARSET_GSM), (byte) (random.nextInt() % 255));
			log.debug("Text message does NEED TO SPLIT");
			if (segments != null) {
				for (byte[] s : segments) {
					SubmitSm request = new SubmitSm();
					request.setSourceAddress(new Address(event.getSourceTon(), event.getSourceNpi(), event.getSource()));
					request.setDestAddress(new Address(event.getDestTon(), event.getDestNpi(), event.getDest()));
					request.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
					request.setShortMessage(s);
					
					if (event.getUssdOp() != null) {
						byte[] ussdOp = HexUtil.toByteArray(event.getUssdOp());
						request.addOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, ussdOp));
						
						if ((event.getSessionActive() != null) && (event.getSessionNumber() != null) && (event.getSequenceNumber() != null)) {
							int e = (( event.getSequenceNumber() << 1 ) | (event.getSessionActive() ? 1 : 0));
							byte[] sessInfo = { event.getSessionNumber().byteValue(), (byte) e };
							request.addOptionalParameter(new Tlv(SmppConstants.TAG_ITS_SESSION_INFO, sessInfo));
						}
						
					}
					
					session.sendRequestPdu(request, 60000, false);
				}
				return;
			} else {
				log.debug("Text message DOES NOT need to split");
				SubmitSm request = new SubmitSm();
				request.setSourceAddress(new Address(event.getSourceTon(), event.getSourceNpi(), event.getSource()));
				request.setDestAddress(new Address(event.getDestTon(), event.getDestNpi(), event.getDest()));
				request.setShortMessage(CharsetUtil.encode(event.getText(), CharsetUtil.CHARSET_GSM));
				
				if (event.getUssdOp() != null) {
					byte[] ussdOp = HexUtil.toByteArray(event.getUssdOp());
					request.addOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, ussdOp));
					
					if ((event.getSessionActive() != null) && (event.getSessionNumber() != null) && (event.getSequenceNumber() != null)){
						int e = (( event.getSequenceNumber() << 1 ) | (event.getSessionActive() ? 1 : 0));
						byte[] sessInfo = { event.getSessionNumber().byteValue(), (byte) e };
						request.addOptionalParameter(new Tlv(SmppConstants.TAG_ITS_SESSION_INFO, sessInfo));
					}
					
				}
				session.sendRequestPdu(request, 60000, false);
				return;
			}
		}
		throw new IOException("SMSC Connection is not ready");
	}

	@PreDestroy
	protected void stopAll() throws Throwable {
		log.debug("== DESTROY ALL CONNECTION TO SMSC ==");
		ReconnectionDaemon.getInstance().shutdown();
		for (Iterator<OutboundClient> it = activeSessions.values().iterator(); it.hasNext(); ) {
			it.next().shutdown();
		}
	}


	public List<SmppSessionConfiguration> getConn() {
		return conn;
	}

	public void setConn(List<SmppSessionConfiguration> conn) {
		this.conn = conn;
	}

	
}
