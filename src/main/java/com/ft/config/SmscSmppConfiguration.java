package com.ft.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Async;

import com.ft.smpp.OutboundClient;
import com.ft.smpp.ReconnectionDaemon;
import com.ft.smpp.impl.UssdClientMessagingService;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.gsm.GsmUtil;
import com.cloudhopper.commons.util.LoadBalancedList;
import com.cloudhopper.commons.util.LoadBalancedLists;
import com.cloudhopper.commons.util.RoundRobinLoadBalancedList;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;

@ConfigurationProperties(prefix = "smpp-connector", ignoreUnknownFields = false)
@ConditionalOnProperty(prefix = "smpp-connector", name = { "conn" })
public class SmscSmppConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SmscSmppConfiguration.class);
	
	public static final Random random = new Random();

	private List<SmppSessionConfiguration> conn;
	
	/**
	 * List of balanced list for SMSC connectors
	 */
	public static final LoadBalancedList<OutboundClient> balancedList = LoadBalancedLists
			.synchronizedList(new RoundRobinLoadBalancedList<OutboundClient>());

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
			log.info("Add new SMSC session: {} -- {}:{}", conn.getSessionId(), conn.getConfiguration().getHost(),
					conn.getConfiguration().getPort());
			balancedList.set(conn, 1);
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
	@Async
	public List<String> sendMessage(String source, String text, String destination) throws Exception {
		List<String> result = new ArrayList<String>();
		OutboundClient next;
		SmppSession session;
		// Block until one session is bound
		if (
				((next = balancedList.getNext()) != null) 
				&& ((session = next.getSession()) != null)
				&& (session.isBound())
		) {
			byte[][] segments = GsmUtil.createConcatenatedBinaryShortMessages(
					CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM), (byte) (random.nextInt() % 255));
			log.debug("Text message does NEED TO SPLIT");
			if (segments != null) {
				for (byte[] s : segments) {
					SubmitSm request = new SubmitSm();
					request.getSourceAddress().setAddress(source);
					request.getDestAddress().setAddress(destination);
					request.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
					request.setShortMessage(s);
					SubmitSmResp resp = session.submit(request, 60000);
					if (resp.getCommandStatus() != SmppConstants.STATUS_OK)
						return null;
					log.info("== SubmitSm concatenate: {}", resp.getMessageId());
					result.add(resp.getMessageId());
				}
			} else {
				log.debug("Text message DOES NOT need to split");
				SubmitSm request = new SubmitSm();
				request.getSourceAddress().setAddress(source);
				request.getDestAddress().setAddress(destination);
				request.setShortMessage(CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM));
				SubmitSmResp resp = session.submit(request, 60000);
				if (resp.getCommandStatus() != SmppConstants.STATUS_OK)
					return null;
				log.info("== SubmitSmResp: {}", resp.getMessageId());
				result.add(resp.getMessageId());
			}
		}
		throw new IOException("SMSC Connection is not ready");
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
	public void sendAsyncMessage(String source, String text, String destination) throws Exception {
		log.info("SubmitSm: {} -> {} : {}", source, destination, text);
		OutboundClient next;
		SmppSession session;
		if (
				((next = balancedList.getNext()) != null) 
				&& ((session = next.getSession()) != null)
				&& (session.isBound())
		) {
			byte[][] segments = GsmUtil.createConcatenatedBinaryShortMessages(
					CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM), (byte) (random.nextInt() % 255));
			log.debug("Text message does NEED TO SPLIT");
			if (segments != null) {
				for (byte[] s : segments) {
					SubmitSm request = new SubmitSm();
					request.getSourceAddress().setAddress(source);
					request.getDestAddress().setAddress(destination);
					request.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
					request.setShortMessage(s);
					session.sendRequestPdu(request, 60000, false);
				}
				return;
			} else {
				log.debug("Text message DOES NOT need to split");
				SubmitSm request = new SubmitSm();
				request.getSourceAddress().setAddress(source);
				request.getDestAddress().setAddress(destination);
				request.setShortMessage(CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM));
				session.sendRequestPdu(request, 60000, false);
				return;
			}
		}
		throw new IOException("SMSC Connection is not ready");
	}
	
	public void sendAsyncMessage(String source, byte oaTon, byte oaNpi, String text, String destination, byte daTon, byte daNpi) throws Exception {
		log.info("SubmitSm: {} -> {} : {}", source, destination, text);
		OutboundClient next;
		SmppSession session;
		if (
				((next = balancedList.getNext()) != null) 
				&& ((session = next.getSession()) != null)
				&& (session.isBound())
		) {
			byte[][] segments = GsmUtil.createConcatenatedBinaryShortMessages(
					CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM), (byte) (random.nextInt() % 255));
			log.debug("Text message does NEED TO SPLIT");
			if (segments != null) {
				for (byte[] s : segments) {
					SubmitSm request = new SubmitSm();
					request.getSourceAddress().setAddress(source);
					request.getSourceAddress().setTon(oaTon);
					request.getSourceAddress().setNpi(oaNpi);
					request.getDestAddress().setAddress(destination);
					request.getDestAddress().setTon(daTon);
					request.getDestAddress().setNpi(daNpi);
					request.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
					request.setShortMessage(s);
					session.sendRequestPdu(request, 60000, false);
				}
				return;
			} else {
				log.debug("Text message DOES NOT need to split");
				SubmitSm request = new SubmitSm();
				request.getSourceAddress().setAddress(source);
				request.getSourceAddress().setTon(oaTon);
				request.getSourceAddress().setNpi(oaNpi);
				request.getDestAddress().setAddress(destination);
				request.getDestAddress().setTon(daTon);
				request.getDestAddress().setNpi(daNpi);
				request.setShortMessage(CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM));
				session.sendRequestPdu(request, 60000, false);
				return;
			}
		}
		throw new IOException("SMSC Connection is not ready");
	}

	@Async
	public static SubmitSmResp sendMessage(SubmitSm request) throws Exception {
		while (true) {
			final OutboundClient next = balancedList.getNext();
			final SmppSession session = next.getSession();
			if (session != null && session.isBound()) {
				return session.submit(request, 60000);
			}
		}
	}

	public static SubmitSm copySubmitSm(SubmitSm src) {
		SubmitSm request = new SubmitSm();
		request.setDestAddress(src.getDestAddress());
		request.setSourceAddress(src.getSourceAddress());
		request.setPriority(src.getPriority());
		request.setProtocolId(src.getProtocolId());
		request.setEsmClass(src.getEsmClass());
		request.setDataCoding(src.getDataCoding());
		request.setRegisteredDelivery(src.getRegisteredDelivery());
		return request;
	}

	@PreDestroy
	protected void stopAll() throws Throwable {
		log.debug("== DESTROY ALL CONNECTION TO SMSC ==");
		ReconnectionDaemon.getInstance().shutdown();
		for (LoadBalancedList.Node<OutboundClient> node : balancedList.getValues()) {
			node.getValue().shutdown();
		}
	}


	public List<SmppSessionConfiguration> getConn() {
		return conn;
	}

	public void setConn(List<SmppSessionConfiguration> conn) {
		this.conn = conn;
	}

	
}
