package com.ft.service.util;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

public class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		HttpRequest wrapper = new HttpRequestWrapper(request);
		for (String k: headers.keySet())
			wrapper.getHeaders().set(k, headers.get(k));
        return execution.execute(wrapper, body);
	}

    private final Map<String, String> headers;


    public HeaderRequestInterceptor(Map<String, String> headers) {
    	this.headers = headers;
    }
}
