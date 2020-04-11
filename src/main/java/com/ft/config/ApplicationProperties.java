package com.ft.config;

import java.util.HashMap;
import java.util.Map;

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
	private String callbackUrl = "http://69.64.81.160:4444/AirtelUssd/MFill"; // ?msisdn=2348022221569&sessionid=15856853985735944&INPUT=5511&code=20035%27";
	
	private String errorText = "Gateway Timeout";
	private Map<String, String> queryParams = new HashMap<>();
	/**
     * Mobile Country Code
     */
    private String mcc = "84";
    
    private String moContinue = "17";
    
    private String moEnd = "32";
    
    private String moErr = "64";
    
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

	public Map<String, String> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String, String> queryParams) {
		this.queryParams = queryParams;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public String getMoContinue() {
		return moContinue;
	}

	public void setMoContinue(String moContinue) {
		this.moContinue = moContinue;
	}

	public String getMoEnd() {
		return moEnd;
	}

	public void setMoEnd(String moEnd) {
		this.moEnd = moEnd;
	}

	public String getMoErr() {
		return moErr;
	}

	public void setMoErr(String moErr) {
		this.moErr = moErr;
	}


	
    
}
