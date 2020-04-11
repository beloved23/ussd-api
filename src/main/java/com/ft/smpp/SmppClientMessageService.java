package com.ft.smpp;

import com.cloudhopper.smpp.pdu.BaseSm;
import com.cloudhopper.smpp.pdu.PduResponse;

public interface SmppClientMessageService {

	PduResponse received(OutboundClient client, @SuppressWarnings("rawtypes") BaseSm request);

}
