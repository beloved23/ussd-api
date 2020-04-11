package com.ft.smpp.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.BaseSm;
/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2014 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.tlv.TlvConvertException;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.cloudhopper.smpp.util.DeliveryReceiptException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ft.config.Constants;
import com.ft.domain.Notification;
import com.ft.service.EventService;
import com.ft.service.dto.EventDTO;
import com.ft.smpp.OutboundClient;
import com.ft.smpp.SmppClientMessageService;

@Service
public class UssdClientMessagingService implements SmppClientMessageService {

	private final Logger log = LoggerFactory.getLogger(UssdClientMessagingService.class);

	@Autowired
	EventService eventHandler;

	/**
	 * delivery receipt, or MO
	 *
	 * @param client
	 * @param deliverSm
	 * @return
	 */
	public PduResponse received(OutboundClient client, BaseSm deliverSm) {
		// Handling MO SMS
		String shortMsg = null;
		String sourceAddress = deliverSm.getSourceAddress().getAddress();
		String destAddress = deliverSm.getDestAddress().getAddress();
		try {
			Tlv messagePayload = deliverSm.getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD);
			if (messagePayload != null) {
				shortMsg = messagePayload.getValueAsString();
			} else {
				shortMsg = CharsetUtil.decode(deliverSm.getShortMessage(), CharsetUtil.CHARSET_GSM);
			}
			log.info("<<< === Got DeliverSM: {} -> {} | {}", sourceAddress, destAddress, shortMsg);
		} catch (Exception e) {
			log.error("Cannot parse message: {}", e);
		}
		try {
			Tlv deliveryReceipt = deliverSm.getOptionalParameter(SmppConstants.TAG_RECEIPTED_MSG_ID);
			if (deliveryReceipt != null) {
				log.debug("<<< === Delivery Report for message ID: {}", deliveryReceipt.getValueAsString());
				updateDeliveryReport(deliveryReceipt.getValueAsString(), HttpStatus.CREATED.value(), destAddress,
						sourceAddress);
			}
		} catch (Exception e) {
		}
		try {
			DeliveryReceipt dlr = DeliveryReceipt.parseShortMessage(shortMsg, DateTimeZone.getDefault(), false);
			log.debug("<<< === Delivery Report: {}", dlr);
			if (dlr.getMessageId() == null) {
				throw new DeliveryReceiptException("Message ID is null!");
			}
			if (dlr.getState() == SmppConstants.STATE_DELIVERED)
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.CREATED.value(), destAddress, sourceAddress);
			else if (dlr.getState() == SmppConstants.STATE_DELETED)
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.NOT_ACCEPTABLE.value(), destAddress, sourceAddress);
			else if (dlr.getState() == SmppConstants.STATE_EXPIRED)
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.GONE.value(), destAddress, sourceAddress);
			else if (dlr.getState() == SmppConstants.STATE_REJECTED)
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.FORBIDDEN.value(), destAddress, sourceAddress);
			else if (dlr.getState() == SmppConstants.STATE_UNDELIVERABLE)
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.EXPECTATION_FAILED.value(), destAddress,
						sourceAddress);
			else // Other delivery states are map as to be done
				updateDeliveryReport(dlr.getMessageId(), HttpStatus.ACCEPTED.value(), destAddress, sourceAddress);
		} catch (DeliveryReceiptException e) {
			log.debug("MO SMS: {} -> {} | {}", sourceAddress, destAddress, shortMsg);
			processMobileOriginatedMessage(sourceAddress, destAddress, shortMsg, deliverSm, client.getSessionId());
		}
		PduResponse response = deliverSm.createResponse();
		response.setResultMessage("OK");
		return response;
	}

	/**
	 * Process the USSD message
	 * 
	 * @param sourceAddress
	 * @param destAddress
	 * @param shortMsg
	 * @param deliverSm
	 */
	public void processMobileOriginatedMessage(String sourceAddress, String destAddress, String shortMsg,
			BaseSm deliverSm, String smppClientId) {
		
		EventDTO evt = new EventDTO();
		evt.setChannel(Constants.CHANNEL_SMS);
		evt.setSource(sourceAddress); evt.setSourceTon(deliverSm.getSourceAddress().getTon()); evt.setSourceNpi(deliverSm.getSourceAddress().getNpi());
		evt.setDest(destAddress); evt.setDestTon(deliverSm.getDestAddress().getTon()); evt.setDestNpi(deliverSm.getDestAddress().getNpi());
		evt.setText(shortMsg);
		
		
		Map<String, String> params = new HashMap<>();
		Tlv ussdOp = deliverSm.getOptionalParameter(SmppConstants.TAG_USSD_SERVICE_OP);
		if (ussdOp != null)
		try {
			int ussdServiceOp = ussdOp.getLength() == 1 ? ussdOp.getValueAsUnsignedByte()
					: ussdOp.getValueAsUnsignedShort();
			params.put("ussd_service_op", ussdServiceOp + "");
			Tlv sessionInfo = deliverSm.getOptionalParameter(SmppConstants.TAG_ITS_SESSION_INFO);
			if (sessionInfo != null) {
				byte[] sessionInfoData = sessionInfo.getValue();
				log.debug("Got session info, length {}", sessionInfo.getLength(), HexUtil.toHexString(sessionInfoData));
				if (sessionInfo.getLength() == 2) {
					int sessionNumber = sessionInfoData[0];
					params.put("sessionNumber", sessionNumber + "");
					int sequenceNumber = sessionInfoData[1] >> 1;
					params.put("sequenceNumber", sequenceNumber + "");
					boolean sessionIndicatorActive = (sessionInfoData[1] & 1) != 0;
					params.put("sessionIndicatorActive", sessionIndicatorActive + "");
					
					// reserve for response
					evt.setSessionNumber(sessionNumber);
					evt.setSequenceNumber(sequenceNumber);
					evt.setSessionActive(sessionIndicatorActive);
				}
				params.put("its_session_info", HexUtil.toHexString(sessionInfoData));
			}

			Tlv networkErrorCode = deliverSm.getOptionalParameter(SmppConstants.TAG_NETWORK_ERROR_CODE);
			if (networkErrorCode != null) {
				byte[] errorCode = networkErrorCode.getValue();
				params.put("network_error_code", HexUtil.toHexString(errorCode));
			}

		} catch (TlvConvertException e) {
			log.error("Cannot extract USSD service op", e);
		}
		
		try {
			eventHandler.messageReceivedWithLog(smppClientId, evt, params);
		} catch (JsonProcessingException e) {
			log.error("Error processing data", e);
		}
	}

	/**
	 * Update the delivery receipt for one SMS
	 * 
	 * @param messageID
	 * @param state
	 * @param mtSource
	 * @param mtDest
	 * @return
	 */
	public Notification updateDeliveryReport(String messageID, Integer state, String mtSource, String mtDest) {
//        // Try to look for this message id
//        Optional<Notification> sms = notificationRepo.findOne(
//            Example.of(
//                new Notification()
//                    .addMeta("messageID", messageID)
//                    .addMeta("momt", "MT")
//                    .addMeta("shortcode", mtSource)
//                    .channel(Constants.CHANNEL_SMS)
//                    .msisdn(mtDest)
//            ));
//        if (sms.isPresent()) {
//            return notificationRepo.save(
//                sms.get()
//                    .state(state)
//            );
//        }
		return null;
	}
	
//	public void process() {
//		if (abort)
//        {
//            var builder = SMS.ForSubmit().From(sessIn.ServiceCode, AddressTON.NetworkSpecific, AddressNPI.Unknown).To(data?.SourceAddr).Text("Error")
//               .Coding((DataCodings)0x15)
//                .ServiceType(data.ServiceType)
//               .AddParameter(OptionalTags.UssdServiceOp, new byte[] { 17 })//PSSR response
//              .AddParameter(OptionalTags.ItsSessionInfo, new byte[] { 0, 1 });
//            client.Submit(builder);
//           /// FileLog.Info("Submit Error");
//        }
//        else
//        {
//            var sessOut = new SessionOutModel();// Gateway.SessionManagerServiceAPI.ProcessReceive(sessIn);
//            //FileLog.Info(Util.SerializeJSON(sessOut) + " from SIRIUS");
//            try
//            {
//                var resp = "0";
//                byte respOut = 2;
//                byte b = 0;
//                 if(sessOut.MsgType == MessageType.End)
//                {
//                    resp = "3";
//                    respOut = 17;
//                    b = 1;
//                }
//                var text = string.Format("1|121:{0}|123:{1}|110:{2}|116:{3}", Util.ToBase64(sessOut.Message),
//                    Util.ToBase64(resp),  Util.ToBase64(sessionId), Util.ToBase64("1"));
//                var builder = SMS.ForSubmit().From(data.DestAddr)
//                    .To(data.SourceAddr)
//                    .Text(text).ServiceType(data.ServiceType).Coding(data.DataCoding).MaxPartSize(208)
//                    .AddParameter(OptionalTags.UssdServiceOp, new byte[] { respOut }) //PSSR response
//                    .AddParameter(OptionalTags.ItsSessionInfo, new byte[] { 0, b});
//
//               var k = client.Submit(builder);
//           
//                foreach (var _k in k)
//                {
//                    //FileLog.Info(Util.SerializeJSON(_k));
//                }
//            }
//            catch (Exception exx)
//            {
//                //FileLog.Error(exx);
//            }
//        }
//    }
}
