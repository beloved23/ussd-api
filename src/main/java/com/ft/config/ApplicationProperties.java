package com.ft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Ussd Api.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
	
	/**
	 * Callback URL for DeliverSM
	 */
	private String callbackUrl;
	
	/**
     * Mobile Country Code
     */
    private String mcc = "84";
    
    /**
     * Return the correct MSISDN format for the whole number
     *
     * @param msisdn
     * @return
     */
    public String msisdn(String msisdn) {
    	try {
	        msisdn = "" + Long.parseLong(msisdn.trim());
	        if (!msisdn.substring(0, getMcc().length()).equals(getMcc())) {
	            msisdn = getMcc() + msisdn;
	        }
	        return String.valueOf(Long.parseLong(msisdn));
    	} catch (Exception e) {
    		return null;
    	}
    }

    /**
     * Return the correct MSISDN format for the whole number
     *
     * @param msisdn
     * @return
     */
    public String isdn(String msisdn) {
        msisdn = "" + Long.parseLong(msisdn.trim());
        if (msisdn.substring(0, getMcc().length()).equalsIgnoreCase(getMcc())) {
            msisdn = msisdn.substring(getMcc().length());
        }
        return String.valueOf(Long.parseLong(msisdn));
    }

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}
    
    
}
